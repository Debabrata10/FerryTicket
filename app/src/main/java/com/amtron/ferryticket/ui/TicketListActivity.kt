package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.adapter.OnTicketsRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.TicketAdapter
import com.amtron.ferryticket.databinding.ActivityTicketListBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.LastTransaction
import com.amtron.ferryticket.model.Ticket
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
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
class TicketListActivity : AppCompatActivity(), OnTicketsRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityTicketListBinding
	private lateinit var ticketsList: ArrayList<Ticket>
	private lateinit var ticketsRecyclerView: RecyclerView
	private lateinit var ticketAdapter: TicketAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTicketListBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()
		ticketsList = ArrayList()

		/*try {
			val recentTicketList = sharedPreferences.getString("recent_tickets", "").toString()
			ticketsList = Gson().fromJson(
				recentTicketList,
				object : TypeToken<List<Ticket>>() {}.type
			)
		} catch (e: Exception) {
			NotificationHelper().getErrorAlert(this@TicketListActivity, "No tickets found")
			startActivity(Intent(this, HomeActivity::class.java))
		}*/
		getRecentTickets(
			Util().getJwtToken(
				sharedPreferences.getString("user", "").toString()
			)
		)

		onBackPressedDispatcher.addCallback(this) {
			startActivity(
				Intent(
					this@TicketListActivity,
					HomeActivity::class.java
				)
			)
		}

		/*binding.pendingTicketsBtn.setOnClickListener {
			getRecentTickets(Util().getJwtToken(sharedPreferences.getString("user", "")), "pendingTickets")
		}*/
	}

	private fun getRecentTickets(token: String) {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting Recent Tickets..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance(this)!!.create(Client::class.java)
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
							val latestTicketsJson = obj.get("latest_tickets") as JSONArray

							//Start for mobile view code
							if (latestTicketsJson.length() > 0) {
								dialog.dismissWithAnimation()
								ticketsList = Gson().fromJson(
									latestTicketsJson.toString(),
									object : TypeToken<List<Ticket>>() {}.type
								)
								ticketAdapter = TicketAdapter(ticketsList)
								ticketAdapter.setOnItemClickListener(this@TicketListActivity)
								ticketsRecyclerView = binding.recentTicketsRecyclerView
								ticketsRecyclerView.adapter = ticketAdapter
								ticketsRecyclerView.layoutManager = LinearLayoutManager(this@TicketListActivity)
								ticketsRecyclerView.isNestedScrollingEnabled = false
							} else {
								dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
								dialog.cancelText = "GO BACK"
								dialog.setOnCancelListener {
									dialog.dismiss()
									startActivity(
										Intent(
											this@TicketListActivity,
											HomeActivity::class.java
										)
									)
								}
							}
							dialog.dismiss()
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@TicketListActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@TicketListActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					dialog.dismiss()
					NotificationHelper().getErrorAlert(this@TicketListActivity, "Server Error")
				}
			})
		}
	}

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		val ticket: Ticket = Gson().fromJson(
			type,
			object : TypeToken<Ticket>() {}.type
		)
		try {
			val lastTransaction : LastTransaction = Gson().fromJson(
				sharedPreferences.getString("last_transaction", "").toString(),
				object : TypeToken<LastTransaction>() {}.type
			)
			if (ticket.order_status != "SUCCESS") {
				if (ticket.ticket_no == lastTransaction.ticketNo) {
					val bundle = Bundle()
					bundle.putString("last_txn", lastTransaction.transactionId)
					val i = Intent(this, LastTxnStatusActivity::class.java)
					i.putExtras(bundle)
					editor.putString("ticket", Gson().toJson(ticket))
					editor.putString("activity_from", "ticketListActivity")
					editor.apply()
					startActivity(i)
				} else if (ticket.mode_of_payment == "CARD-P" || ticket.mode_of_payment == "CARD-O") {
					val bundle = Bundle()
					bundle.putString("info", "check_status")
					val i = Intent(this, TicketActivity::class.java)
					i.putExtras(bundle)
					editor.putString("ticket", Gson().toJson(ticket))
					editor.putString("activity_from", "ticketListActivity")
					editor.apply()
					startActivity(i)
				} else {
					editor.putString("ticket", Gson().toJson(ticket))
					editor.putString("activity_from", "ticketListActivity")
					editor.apply()
					startActivity(Intent(this@TicketListActivity, TicketActivity::class.java))
				}
			} else {
				editor.putString("ticket", Gson().toJson(ticket))
				editor.putString("activity_from", "ticketListActivity")
				editor.apply()
				startActivity(Intent(this@TicketListActivity, TicketActivity::class.java))
			}
		}catch (e: Exception) {
			Log.d("exception", "null last transaction")
			editor.putString("ticket", Gson().toJson(ticket))
			editor.putString("activity_from", "ticketListActivity")
			editor.apply()
			startActivity(Intent(this@TicketListActivity, TicketActivity::class.java))
		}
	}
}