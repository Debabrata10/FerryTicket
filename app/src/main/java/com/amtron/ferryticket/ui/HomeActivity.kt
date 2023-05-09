package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.AssignedRoutesAdapter
import com.amtron.ferryticket.adapter.OnAssignedRoutesRecyclerViewItemClickListener
import com.amtron.ferryticket.databinding.ActivityHomeBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
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
class HomeActivity : AppCompatActivity(),
	OnAssignedRoutesRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: Editor
	private lateinit var binding: ActivityHomeBinding
	private lateinit var sourceDestinationRecyclerView: RecyclerView
	private lateinit var assignedRoutesAdapter: AssignedRoutesAdapter
	private lateinit var dialog: SweetAlertDialog
	//For Tablet View
//	private lateinit var recentTicketAdapter: TicketAdapter
//	private lateinit var recentTicketsRecyclerView: RecyclerView

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityHomeBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		//check if proxy settings is present
		try {
			val proxyPort = sharedPreferences.getString("proxy_port", "").toString();
			val proxyIp = sharedPreferences.getString("proxy_ip", "").toString();
			if (proxyIp.isEmpty() || proxyPort.isEmpty()) {
				val noProxySettingsFoundAlert = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
				noProxySettingsFoundAlert.setCancelable(false)
				noProxySettingsFoundAlert.titleText = "WARNING!"
				noProxySettingsFoundAlert.contentText = "Please define proxy settings first"
				noProxySettingsFoundAlert.confirmText = "OK"
				noProxySettingsFoundAlert.cancelText = "CANCEL"
				noProxySettingsFoundAlert.show()
				noProxySettingsFoundAlert.setConfirmClickListener {
					startActivity(Intent(this, ProxyActivity::class.java))
				}
				noProxySettingsFoundAlert.setCancelClickListener {
					finishAffinity()
				}
			}
		} catch (e: Exception) {
			val noProxySettingsFoundAlert = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
			noProxySettingsFoundAlert.setCancelable(false)
			noProxySettingsFoundAlert.titleText = "WARNING!"
			noProxySettingsFoundAlert.contentText = "Please define proxy settings first"
			noProxySettingsFoundAlert.confirmText = "OK"
			noProxySettingsFoundAlert.cancelText = "CANCEL"
			noProxySettingsFoundAlert.show()
			noProxySettingsFoundAlert.setConfirmClickListener {
				startActivity(Intent(this, ProxyActivity::class.java))
			}
			noProxySettingsFoundAlert.setCancelClickListener {
				finishAffinity()
			}
		}

		getMasterData(Util().getJwtToken(sharedPreferences.getString("user", "").toString()))

		binding.appLogo.setOnClickListener {// refresh app to get master data and home data
			startActivity(intent)
		}

		binding.profileCard.setOnClickListener {
			startActivity(
				Intent(this, ProfileActivity::class.java)
			)
		}

		binding.posSettings.setOnClickListener {
			val posPasswordBottomSheet = BottomSheetDialog(this@HomeActivity)
			posPasswordBottomSheet.setCancelable(false)
			posPasswordBottomSheet.setContentView(R.layout.enter_device_serial_number_layout)
			val masterPassword = posPasswordBottomSheet.findViewById<TextView>(R.id.serial_number)
			val verifyBtn =
				posPasswordBottomSheet.findViewById<MaterialButton>(R.id.btn_getTid)
			val textInputLayout =
				posPasswordBottomSheet.findViewById<TextInputLayout>(R.id.textInputLayout)
			textInputLayout!!.hint = "Enter Master Password"
			masterPassword!!.inputType = InputType.TYPE_CLASS_NUMBER
			masterPassword.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
			masterPassword.imeOptions = EditorInfo.IME_ACTION_DONE
			verifyBtn!!.text = "VERIFY"
			val cancelBtn = posPasswordBottomSheet.findViewById<MaterialButton>(R.id.btn_cancel)
			posPasswordBottomSheet.show()

			verifyBtn.setOnClickListener {
				if (masterPassword.text.toString() == "123") {
					posPasswordBottomSheet.dismiss()
					val bundle = Bundle()
					bundle.putString("pos_settings", "yes")
					val i = Intent(this, PosActivity::class.java)
					i.putExtras(bundle)
					startActivity(i)
				} else {
					masterPassword.text = null
					Toast.makeText(this, "incorrect password", Toast.LENGTH_SHORT).show()
				}
			}

			cancelBtn!!.setOnClickListener {
				posPasswordBottomSheet.dismiss()
			}
		}

		binding.tickets.setOnClickListener {
			startActivity(Intent(this, TicketListActivity::class.java))
		}

		/*binding.recentFerry.ferryCard.setOnClickListener {
			//send ferry
			val i = Intent(this, BookActivity::class.java)
			startActivity(i)
		}*/

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
				exitBottomSheet.dismiss()
				finishAffinity()
			}
			cancel?.setOnClickListener { exitBottomSheet.dismiss() }
		}
	}

	private fun getMasterData(token: String) {
		dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting Master Data..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance(this@HomeActivity)!!.create(Client::class.java)
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
						helper.responseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							dialog.titleText = "Master Data Fetched"
							val masterData: MasterData = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<MasterData>() {}.type
							)
							editor.putString("masterData", Gson().toJson(masterData))
							editor.apply()

							getHomeData(
								Util().getJwtToken(
									sharedPreferences.getString("user", "").toString()
								)
							)
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
		dialog.titleText = "Loading.."
		val api = RetrofitHelper.getInstance(this@HomeActivity)!!.create(Client::class.java)
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
						helper.responseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							val obj = JSONObject(helper.getDataAsString())
							binding.ticketsToday.text = (obj.get("todays_ticket") as Int).toString()

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
									LinearLayoutManager(this@HomeActivity)
								sourceDestinationRecyclerView.isNestedScrollingEnabled = false
								binding.srcDestRecyclerView.visibility = View.VISIBLE
								binding.noRoutesCard.visibility = View.GONE
							} else {
								binding.srcDestRecyclerView.visibility = View.GONE
								binding.noRoutesCard.visibility = View.VISIBLE
							}

							//Recent ferry code
							/*if (!JSONObject.NULL.equals(obj.get("recent_ferry"))) {
								val recentFerryJson = obj.get("recent_ferry") as JSONObject
								val recentFerry: Ferry = Gson().fromJson(
									recentFerryJson.toString(),
									object : TypeToken<Ferry>() {}.type
								)
								binding.recentFerry.ferryCard.visibility = View.VISIBLE
								binding.noRecentFerryCard.visibility = View.GONE
								binding.recentFerry.ferryName.text = recentFerry.ferry_name
								binding.recentFerry.ferryNumber.text = recentFerry.ferry_no
								binding.recentFerry.departureTime.text = recentFerry.departure
								binding.recentFerry.arrivalTime.text = recentFerry.arrival
								binding.recentFerry.src.text = recentFerry.source.ghat_name
								binding.recentFerry.dest.text = recentFerry.destination.ghat_name
								binding.recentFerry.availablePerson.text =
									recentFerry.seat_capacity
								binding.recentFerry.availableCycle.text =
									recentFerry.bicycle_capacity
								binding.recentFerry.availableMotorcycle.text =
									recentFerry.bicycle_capacity
								binding.recentFerry.availableLmv.text =
									recentFerry.four_wheeler_lmv_capacity
								binding.recentFerry.availableHmv.text =
									recentFerry.four_wheeler_hmv_capacity
								binding.recentFerry.availableGoods.text =
									recentFerry.others_capacity.toString()
								binding.recentFerry.ferryCard.setOnClickListener {
									getFerryService(recentFerry.id)
								}
								binding.recentFerry.ferryCard.visibility = View.VISIBLE
								binding.noRecentFerryCard.visibility = View.GONE
							} else {
								binding.recentFerry.ferryCard.visibility = View.GONE
								binding.noRecentFerryCard.visibility = View.VISIBLE
							}*/

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

	private fun getFerryService(id: Int) {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Loading..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance(this@HomeActivity)!!.create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getService(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				id
			)
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {
						val helper = ResponseHelper()
						helper.responseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							dialog.dismiss()
							val ferryService: FerryService = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<FerryService>() {}.type
							)
							editor.putString("service", Gson().toJson(ferryService))
							editor.apply()
							startActivity(
								Intent(this@HomeActivity, BookActivity::class.java)
							)
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

	override fun onAssignedRoutesItemClickListener(
		position: Int,
		type: String,
		requirement: String
	) {
		if (requirement == "getFerries") {
			dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
			dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
			dialog.titleText = "Getting Services"
			dialog.setCancelable(false)
			dialog.show()
			val ferry: AssignedRoutes = Gson().fromJson(
				type,
				object : TypeToken<AssignedRoutes>() {}.type
			)
			val api = RetrofitHelper.getInstance(this@HomeActivity)!!.create(Client::class.java)
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
							helper.responseHelper(response.body())
							if (helper.isStatusSuccessful()) {
								dialog.dismiss()
								val ferryServiceList: ArrayList<FerryService> = Gson().fromJson(
									helper.getDataAsString(),
									object : TypeToken<List<FerryService>>() {}.type
								)
								editor.putString("activity_from", "")
								editor.putString("sourceGhat", ferry.source_ghat_name)
								editor.putString("destinationGhat", ferry.destination_ghat_name)
								editor.putString("ferryServices", Gson().toJson(ferryServiceList))
								editor.apply()
								startActivity(
									Intent(
										this@HomeActivity,
										FerryListActivity::class.java
									)
								)
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
		} else {
			dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
			dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
			dialog.titleText = "Getting Report"
			dialog.setCancelable(false)
			dialog.show()
			val ferry: AssignedRoutes = Gson().fromJson(
				type,
				object : TypeToken<AssignedRoutes>() {}.type
			)
			val api = RetrofitHelper.getInstance(this@HomeActivity)!!.create(Client::class.java)
			GlobalScope.launch {
				val call: Call<JsonObject> = api.getReport(
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
							helper.responseHelper(response.body())
							if (helper.isStatusSuccessful()) {
								dialog.dismiss()
								val reportsList: ArrayList<Report> = Gson().fromJson(
									helper.getDataAsString(),
									object : TypeToken<List<Report>>() {}.type
								)
								editor.putString("sourceGhat", ferry.source_ghat_name)
								editor.putString("destinationGhat", ferry.destination_ghat_name)
								editor.putString("reports", Gson().toJson(reportsList))
								editor.apply()
								startActivity(
									Intent(
										this@HomeActivity,
										ReportActivity::class.java
									)
								)
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
}