package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.PassengerDetails

class PassengerDetailsTicketViewAdapter(private val passengerList: List<PassengerDetails>) :
	RecyclerView.Adapter<PassengerDetailsTicketViewAdapter.ViewHolder>() {
	private lateinit var passenger: PassengerDetails

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view =
			layoutInflater.inflate(R.layout.passenger_details_inside_ticket_view, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		passenger = passengerList[position]

		holder.passengerDetails.text =
			"${passenger.passenger_name} (${passenger.passenger_type.type} ${passenger.gender.gender_name}, ${passenger.age})"
		if (passenger.is_ph == 1) {
			holder.handicap.text = "(differently abled)"
		} else {
			holder.handicap.visibility = View.GONE
		}
	}

	override fun getItemCount(): Int {
		return passengerList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val passengerDetails: TextView = itemView.findViewById(R.id.passenger_name_gender_age)
		val handicap: TextView = itemView.findViewById(R.id.passenger_ph)
	}
}