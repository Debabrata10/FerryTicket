package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.model.Ticket
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class TicketAdapter(private val ticketList: List<Ticket>) :
	RecyclerView.Adapter<TicketAdapter.ViewHolder>() {
	private lateinit var mItemClickListener: OnTicketsRecyclerViewItemClickListener
	private lateinit var ticket: Ticket

	fun setOnItemClickListener(mItemClickListener: OnTicketsRecyclerViewItemClickListener?) {
		this.mItemClickListener = mItemClickListener!!
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.recent_ticket_layout, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		ticket = ticketList[position]

		holder.ferryName.text = ticket.ferry.ferry_name
		holder.ticketNumber.text = ticket.ticket_no
		holder.date.text = DateAndTimeHelper().changeDateFormat("dd MMM, yyyy", ticket.ferry_date)
		holder.departureTime.text = DateAndTimeHelper().changeTimeFormat(ticket.fs_departure_time)
		holder.arrivalTime.text = DateAndTimeHelper().changeTimeFormat(ticket.fs_reached_time)
		if (ticket.order_status == "PENDING") {
			holder.ticket.setBackgroundColor(Color.parseColor("#ffb3b3"))
		}
		holder.ticket.setOnClickListener {
			mItemClickListener.onRecentTicketsItemClickListener(position, Gson().toJson(ticketList[position]))
		}
	}

	override fun getItemCount(): Int {
		return ticketList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val ticket: MaterialCardView = itemView.findViewById(R.id.ticket)
		val ticketNumber: TextView = itemView.findViewById(R.id.ticket_number)
		val date: TextView = itemView.findViewById(R.id.date)
		val departureTime: TextView = itemView.findViewById(R.id.departure_time)
		val arrivalTime: TextView = itemView.findViewById(R.id.arrival_time)
		val ferryName: TextView = itemView.findViewById(R.id.ferry_name)
	}
}