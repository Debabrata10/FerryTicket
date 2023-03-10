package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
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

		holder.ticketNumber.text = ticket.ticket_no
		holder.date.text = ticket.ferry_date
		holder.price.text = "₹${ticket.total_amt}"
		holder.ticket.setOnClickListener {
			mItemClickListener.onRecentTicketsItemClickListener(position, Gson().toJson(ticket))
		}
	}

	override fun getItemCount(): Int {
		return ticketList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val ticket: MaterialCardView = itemView.findViewById(R.id.ticket)
		val ticketNumber: TextView = itemView.findViewById(R.id.ticket_number)
		val date: TextView = itemView.findViewById(R.id.date)
		val price: TextView = itemView.findViewById(R.id.price)
	}
}