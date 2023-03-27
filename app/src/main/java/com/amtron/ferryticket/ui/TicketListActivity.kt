package com.amtron.ferryticket.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.adapter.OnTicketsRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.TicketAdapter
import com.amtron.ferryticket.databinding.ActivityTicketListBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.model.Ticket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi

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

		try {
			val recentTicketList = sharedPreferences.getString("recent_tickets", "").toString()
			ticketsList = Gson().fromJson(
				recentTicketList,
				object : TypeToken<List<Ticket>>() {}.type
			)
		} catch (e: Exception) {
			NotificationHelper().getErrorAlert(this@TicketListActivity, "No tickets found")
			startActivity(Intent(this, HomeActivity::class.java))
		}
		ticketAdapter = TicketAdapter(ticketsList)
		ticketAdapter.setOnItemClickListener(this@TicketListActivity)
		ticketsRecyclerView = binding.recentTicketsRecyclerView
		ticketsRecyclerView.adapter = ticketAdapter
		ticketsRecyclerView.layoutManager = LinearLayoutManager(this)
		ticketsRecyclerView.isNestedScrollingEnabled = false

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

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		val ticket: Ticket = Gson().fromJson(
			type,
			object : TypeToken<Ticket>() {}.type
		)
		editor.putString("ticket", Gson().toJson(ticket))
		editor.putString("activity_from", "ticketListActivity")
		editor.apply()
		startActivity(Intent(this@TicketListActivity, TicketActivity::class.java))
	}
}