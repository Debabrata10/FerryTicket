package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.adapter.OnTicketsRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.TicketAdapter
import com.amtron.ferryticket.databinding.ActivityTicketListBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
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

		val recentTicketList = sharedPreferences.getString("recent_tickets", "").toString()
		ticketsList = Gson().fromJson(
			recentTicketList,
			object : TypeToken<List<Ticket>>() {}.type
		)
		if (ticketsList.isEmpty()) {
			NotificationHelper().getErrorAlert(this@TicketListActivity, "No tickets found")
		}
		ticketAdapter = TicketAdapter(ticketsList)
		ticketAdapter.setOnItemClickListener(this@TicketListActivity)
		ticketsRecyclerView = binding.recentTicketsRecyclerView
		ticketsRecyclerView.adapter = ticketAdapter
		ticketsRecyclerView.layoutManager = LinearLayoutManager(this)
		ticketsRecyclerView.isNestedScrollingEnabled = false

		binding.recentTicketsBtn.setOnClickListener {
			getRecentTickets(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				"recentTickets"
			)
		}

		/*binding.pendingTicketsBtn.setOnClickListener {
			getRecentTickets(Util().getJwtToken(sharedPreferences.getString("user", "")), "pendingTickets")
		}*/
	}

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		val ticket: Ticket = Gson().fromJson(
			type,
			object : TypeToken<Ticket>() {}.type
		)
		editor.putString("ticket", Gson().toJson(ticket))
		editor.apply()
		startActivity(Intent(this@TicketListActivity, TicketActivity::class.java))
	}

	private fun getRecentTickets(token: String, ticketsType: String) {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting tickets"
		dialog.setCancelable(false)
		dialog.show()
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
							val latestTicketsJson =
								if (ticketsType == "recentTickets") obj.get("latest_tickets") as JSONArray else obj.get(
									"pending_tickets"
								) as JSONArray
							val latestTicketsList: ArrayList<Ticket> = Gson().fromJson(
								latestTicketsJson.toString(),
								object : TypeToken<List<Ticket>>() {}.type
							)
							ticketAdapter = TicketAdapter(latestTicketsList)
							ticketAdapter.setOnItemClickListener(this@TicketListActivity)
							ticketsRecyclerView = binding.recentTicketsRecyclerView
							ticketsRecyclerView.adapter = ticketAdapter
							ticketsRecyclerView.layoutManager =
								LinearLayoutManager(this@TicketListActivity)
							ticketsRecyclerView.isNestedScrollingEnabled = false

							dialog.titleText = "All data fetched successfully"
							dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
							dialog.confirmText = "OK"
							dialog.setConfirmClickListener { dialog.dismiss() }
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
}