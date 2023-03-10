package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.Vehicle

class VehicleDetailsTicketViewAdapter(private val vehicleList: List<Vehicle>) :
	RecyclerView.Adapter<VehicleDetailsTicketViewAdapter.ViewHolder>() {
	private lateinit var vehicle: Vehicle

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view =
			layoutInflater.inflate(R.layout.vehicle_details_inside_ticket_view, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		vehicle = vehicleList[position]

		holder.vehicleDetails.text = "${vehicle.vehicle_type.p_name} (${vehicle.reg_no})"
	}

	override fun getItemCount(): Int {
		return vehicleList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val vehicleDetails: TextView = itemView.findViewById(R.id.vehicle_type_name_registration)
	}
}