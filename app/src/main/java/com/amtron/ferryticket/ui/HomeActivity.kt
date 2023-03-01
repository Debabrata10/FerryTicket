package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.AssignedRoutesAdapter
import com.amtron.ferryticket.adapter.OnAssignedRoutesRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.OnRecentTicketsRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.RecentTicketAdapter
import com.amtron.ferryticket.databinding.ActivityHomeBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class HomeActivity : AppCompatActivity(), OnRecentTicketsRecyclerViewItemClickListener,
	OnAssignedRoutesRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: Editor
	private lateinit var binding: ActivityHomeBinding
	private lateinit var recentTicketsLayoutManager: LinearLayoutManager
	private lateinit var srcDestLayoutManager: LinearLayoutManager
	private lateinit var sourceDestinationRecyclerView: RecyclerView
	private lateinit var recentTicketsRecyclerView: RecyclerView
	private lateinit var assignedRoutesAdapter: AssignedRoutesAdapter
	private lateinit var recentTicketAdapter: RecentTicketAdapter
	private lateinit var dialog: SweetAlertDialog

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityHomeBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		getMasterData(Util().getJwtToken(sharedPreferences.getString("user", "").toString()))

		recentTicketsLayoutManager =
			LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
		srcDestLayoutManager = LinearLayoutManager(this)

		binding.profileCard.setOnClickListener {
			startActivity(
				Intent(this, ProfileActivity::class.java)
			)
		}

		binding.recentFerry.ferryCard.setOnClickListener {
			//send ferry
			val i = Intent(this, BookActivity::class.java)
			startActivity(i)
		}

		onBackPressedDispatcher.addCallback(this) {
			val exitBottomSheet = BottomSheetDialog(this@HomeActivity)
			exitBottomSheet.setCancelable(false)
			exitBottomSheet.setContentView(R.layout.exit_bottom_sheet_layout)
			val title = exitBottomSheet.findViewById<TextView>(R.id.title)
			val header = exitBottomSheet.findViewById<TextView>(R.id.header)
			val success = exitBottomSheet.findViewById<Button>(R.id.success)
			val cancel = exitBottomSheet.findViewById<Button>(R.id.cancel)
			title?.text = "Are you sure you want to exit the app?"
			header?.text = "EXIT?"
			success?.text = "EXIT"
			cancel?.text = "CANCEL"
			exitBottomSheet.show()

			success?.setOnClickListener {
				finishAffinity()
			}
			cancel?.setOnClickListener { exitBottomSheet.dismiss() }
		}
	}

	private fun getMasterData(token: String) {
		dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0");
		dialog.titleText = "Getting Master Data..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getMasterData(token)
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {
						val helper = ResponseHelper()
						helper.ResponseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							dialog.titleText = "Master Data Fetched"
							val masterData: MasterData = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<MasterData>() {}.type
							)
							editor.putString("masterData", Gson().toJson(masterData))
							editor.apply()

							getHomeData(Util().getJwtToken(sharedPreferences.getString("user", "").toString()))
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
					dialog.dismiss()
				}
			})
		}
	}

	private fun getHomeData(token: String) {
		dialog.titleText = "Getting routes and recents"
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getHomeData(token)
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {
						val helper = ResponseHelper()
						helper.ResponseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							val obj = JSONObject(helper.getDataAsString())
							binding.ticketsToday.text = (obj.get("todays_ticket") as Int).toString()
							try {
								val assignedRoutesJson = obj.get("assigned_routes") as JSONArray
								if (assignedRoutesJson.length() > 0) {
									val assignedRoutesList: ArrayList<AssignedRoutes> =
										Gson().fromJson(
											assignedRoutesJson.toString(),
											object : TypeToken<List<AssignedRoutes>>() {}.type
										)
									assignedRoutesAdapter =
										AssignedRoutesAdapter(assignedRoutesList)
									assignedRoutesAdapter.setOnItemClickListener(this@HomeActivity)
									sourceDestinationRecyclerView = binding.srcDestRecyclerView
									sourceDestinationRecyclerView.adapter = assignedRoutesAdapter
									sourceDestinationRecyclerView.layoutManager =
										srcDestLayoutManager
									sourceDestinationRecyclerView.isNestedScrollingEnabled = false
									binding.srcDestRecyclerView.visibility = View.VISIBLE
									binding.noRoutesCard.visibility = View.GONE
								} else {
									binding.srcDestRecyclerView.visibility = View.GONE
									binding.noRoutesCard.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.srcDestRecyclerView.visibility = View.GONE
								binding.noRoutesCard.visibility = View.VISIBLE
							}
							try {
								val latestTicketsJson = obj.get("latest_tickets") as JSONArray
								if (latestTicketsJson.length() > 0) {
									val latestTicketsList: ArrayList<Ticket> = Gson().fromJson(
										latestTicketsJson.toString(),
										object : TypeToken<List<Ticket>>() {}.type
									)
									recentTicketAdapter = RecentTicketAdapter(latestTicketsList)
									recentTicketsRecyclerView = binding.recentTicketsRecyclerView
									recentTicketsRecyclerView.adapter = recentTicketAdapter
									recentTicketsRecyclerView.layoutManager =
										recentTicketsLayoutManager
									recentTicketsRecyclerView.isNestedScrollingEnabled = false
									binding.recentTicketsRecyclerView.visibility = View.VISIBLE
									binding.noRecentTicketsCard.visibility = View.GONE
								} else {
									binding.recentTicketsRecyclerView.visibility = View.GONE
									binding.noRecentTicketsCard.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.recentTicketsRecyclerView.visibility = View.GONE
								binding.noRecentTicketsCard.visibility = View.VISIBLE
							}
							try {
								val recentFerryJson = obj.get("recent_ferry") as String
								if (recentFerryJson.isNotEmpty()) {
									val recentFerry: Ferry = Gson().fromJson(
										recentFerryJson,
										object : TypeToken<Ferry>() {}.type
									)
									binding.recentFerry.ferryCard.visibility = View.VISIBLE
									binding.noRecentFerryCard.visibility = View.GONE
								} else {
									binding.recentFerry.ferryCard.visibility = View.GONE
									binding.noRecentFerryCard.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.recentFerry.ferryCard.visibility = View.GONE
								binding.noRecentFerryCard.visibility = View.VISIBLE
							}

							dialog.titleText = "All data fetched successfully"
							dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
							dialog.confirmText = "OK"
							dialog.setConfirmClickListener { dialog.dismiss() }
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					dialog.dismiss()
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
				}
			})
		}
	}

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		TODO("Not yet implemented")
	}

	override fun onAssignedRoutesItemClickListener(position: Int, type: String) {
		dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0");
		dialog.titleText = "Getting Services"
		dialog.setCancelable(false)
		dialog.show()
		val bundle = Bundle()
		val i = Intent(this@HomeActivity, FerryListActivity::class.java)
		val ferry: AssignedRoutes = Gson().fromJson(
			type,
			object : TypeToken<AssignedRoutes>() {}.type
		)
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getFerries(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				ferry.route_id
			)
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {
						val helper = ResponseHelper()
						helper.ResponseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							dialog.dismiss()
							val ferryServiceList: ArrayList<FerryService> = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<List<FerryService>>() {}.type
							)
							bundle.putString("sourceGhat", ferry.source_ghat_name)
							bundle.putString("destinationGhat", ferry.destination_ghat_name)
							bundle.putString("ferryServices", Gson().toJson(ferryServiceList))
							i.putExtras(bundle)
							startActivity(i)
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					dialog.dismiss()
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
				}
			})
		}
	}
}