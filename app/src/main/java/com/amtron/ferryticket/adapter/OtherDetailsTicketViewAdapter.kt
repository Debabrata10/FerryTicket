package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.Others

class OtherDetailsTicketViewAdapter(private val otherList: List<Others>) :
	RecyclerView.Adapter<OtherDetailsTicketViewAdapter.ViewHolder>() {
	private lateinit var others: Others

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.other_details_inside_ticket_view, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		others = otherList[position]

		holder.otherDetails.text = "${others.other_name} (${others.other_type.p_name})"
		holder.quantity.text = "(${others.quantity})"
	}

	override fun getItemCount(): Int {
		return otherList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val otherDetails: TextView = itemView.findViewById(R.id.other_name_type)
		val quantity: TextView = itemView.findViewById(R.id.other_quantity)
	}
}