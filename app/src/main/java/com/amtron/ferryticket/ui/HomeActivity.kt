package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityHomeBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		getMasterData(Util().getJwtToken(sharedPreferences.getString("user", "").toString()))

		getHomeData(Util().getJwtToken(sharedPreferences.getString("user", "").toString()))

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
							val masterData: MasterData = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<MasterData>() {}.type
							)
							editor.putString("masterData", Gson().toJson(masterData))
							editor.apply()
						} else {
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
				}
			})
		}
	}

	private fun getHomeData(token: String) {
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
									binding.noRoutesText.visibility = View.GONE
								} else {
									binding.srcDestRecyclerView.visibility = View.GONE
									binding.noRoutesText.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.srcDestRecyclerView.visibility = View.GONE
								binding.noRoutesText.visibility = View.VISIBLE
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
									binding.noRecentTicketsText.visibility = View.GONE
								} else {
									binding.recentTicketsRecyclerView.visibility = View.GONE
									binding.noRecentTicketsText.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.recentTicketsRecyclerView.visibility = View.GONE
								binding.noRecentTicketsText.visibility = View.VISIBLE
							}
							try {
								val recentFerryJson = obj.get("recent_ferry") as String
								if (recentFerryJson.isNotEmpty()) {
									val recentFerry: Ferry = Gson().fromJson(
										recentFerryJson,
										object : TypeToken<Ferry>() {}.type
									)
									binding.recentFerry.ferryCard.visibility = View.VISIBLE
									binding.noRecentFerryText.visibility = View.GONE
								} else {
									binding.recentFerry.ferryCard.visibility = View.GONE
									binding.noRecentFerryText.visibility = View.VISIBLE
								}
							} catch (e: Exception) {
								binding.recentFerry.ferryCard.visibility = View.GONE
								binding.noRecentFerryText.visibility = View.VISIBLE
							}
						} else {
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
				}
			})
		}
	}

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		TODO("Not yet implemented")
	}

	override fun onAssignedRoutesItemClickListener(position: Int, type: String) {
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
							NotificationHelper().getErrorAlert(
								this@HomeActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						NotificationHelper().getErrorAlert(
							this@HomeActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(this@HomeActivity, "Server Error")
				}
			})
		}
	}
}