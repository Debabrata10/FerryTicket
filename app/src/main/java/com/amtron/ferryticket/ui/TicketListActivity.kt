package com.amtron.ferryticket.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
	private lateinit var binding: ActivityTicketListBinding
	private lateinit var ticketsList: ArrayList<Ticket>
	private lateinit var ticketsRecyclerView: RecyclerView
	private lateinit var ticketAdapter: TicketAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTicketListBinding.inflate(layoutInflater)
		setContentView(binding.root)

		ticketsList = ArrayList()

		val bundleString = intent.extras
		if (bundleString!!.isEmpty) {
			Toast.makeText(this, "No tickets found", Toast.LENGTH_SHORT).show()
			startActivity(Intent(this, HomeActivity::class.java))
		}

		try {
			val recentTicketList = bundleString.getString("recent_tickets")
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
		} catch (e: Exception) {
			Log.d("Recent tickets", "not found")
		}

		try {
			val pendingTicketList = bundleString.getString("pending_tickets")
			ticketsList = Gson().fromJson(
				pendingTicketList,
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
		} catch (e: Exception) {
			Log.d("Pending tickets", "not found")
		}
	}

	override fun onRecentTicketsItemClickListener(position: Int, type: String) {
		val ticket: Ticket = Gson().fromJson(
			type,
			object : TypeToken<Ticket>() {}.type
		)
		val bundle = Bundle()
		val i = Intent(this@TicketListActivity, TicketActivity::class.java)
		bundle.putString("ticket", Gson().toJson(ticket))
		i.putExtras(bundle)
		startActivity(i)
	}
}