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

class RecentTicketAdapter(private val recentTicketList: List<Ticket>) :
    RecyclerView.Adapter<RecentTicketAdapter.ViewHolder>() {
    private lateinit var mItemClickListener: OnRecentTicketsRecyclerViewItemClickListener
    private lateinit var ticket: Ticket

    fun setOnItemClickListener(mItemClickListener: OnRecentTicketsRecyclerViewItemClickListener?) {
        this.mItemClickListener = mItemClickListener!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recent_ticket_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ticket = recentTicketList[position]

        holder.ticketNumber.text = ticket.ticket_number
        holder.name.text = ticket.name
        holder.date.text = ticket.ticket_date
        holder.ticket.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return recentTicketList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ticket: MaterialCardView = itemView.findViewById(R.id.ticket)
        val ticketNumber: TextView = itemView.findViewById(R.id.ticket_number)
        val name: TextView = itemView.findViewById(R.id.name)
        val date: TextView = itemView.findViewById(R.id.date)
    }
}