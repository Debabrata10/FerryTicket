package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.model.Report

class ReportAdapter(private val reportList: List<Report>) :
	RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
	private lateinit var report: Report

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.report_layout, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		report = reportList[position]

		holder.ferryName.text = report.ferry_name
		holder.ferryNumber.text = report.ferry_code
		holder.departureTime.text =
			DateAndTimeHelper().changeTimeFormat(report.departure_time)
		holder.arrivalTime.text = DateAndTimeHelper().changeTimeFormat(report.reached_time)
		holder.source.text = report.s_name
		holder.destination.text = report.d_name
		holder.availablePerson.text = report.seat_capacity
		holder.availableBicycle.text = report.bicycle_capacity
		holder.availableTwoWheeler.text = report.two_wheeler
		holder.availableLmv.text = report.four_wheeler_lmv_capacity
		holder.availableHmv.text = report.four_wheeler_hmv_capacity
		holder.availableGoods.text = report.others_capacity.toString()
		holder.userBookedTicket.text = report.user_booked_ticket.toString()
		holder.userBookedPerson.text = report.user_booked_person.toString()
		holder.userBookedBicycle.text = report.user_booked_bicycle.toString()
		holder.userBookedTwoWheeler.text = report.user_booked_two_wheeler.toString()
		holder.userBookedFourWheeler.text = report.user_booked_four_wheeler.toString()
		holder.userBookedOthersCapacity.text = report.user_booked_others_capacity.toString()
		holder.operatorBookedTicket.text = report.operator_booked_ticket.toString()
		holder.operatorBookedPerson.text = report.operator_booked_person.toString()
		holder.operatorBookedBicycle.text = report.operator_booked_bicycle.toString()
		holder.operatorBookedTwoWheeler.text = report.operator_booked_two_wheeler.toString()
		holder.operatorBookedFourWheeler.text = report.operator_booked_four_wheeler.toString()
		holder.operatorBookedOthersCapacity.text = report.operator_booked_others_capacity.toString()
		holder.bookedTicket.text = report.booked_ticket.toString()
		holder.bookedPerson.text = report.booked_person.toString()
		holder.bookedBicycle.text = report.booked_bicycle.toString()
		holder.bookedTwoWheeler.text = report.booked_two_wheeler.toString()
		holder.bookedFourWheeler.text = report.booked_four_wheeler.toString()
		holder.bookedOthersCapacity.text = report.booked_others_capacity.toString()
		holder.selfBookedTicket.text = report.self_booked_ticket.toString()
		holder.selfBookedPerson.text = report.self_booked_person.toString()
		holder.selfBookedBicycle.text = report.self_booked_bicycle.toString()
		holder.selfBookedTwoWheeler.text = report.self_booked_two_wheeler.toString()
		holder.selfBookedFourWheeler.text = report.self_booked_four_wheeler.toString()
		holder.selfBookedOthersCapacity.text = report.self_booked_others_capacity.toString()
		holder.selfOcBookedTicket.text = report.self_oc_booked_ticket.toString()
		holder.selfOcBookedPerson.text = report.self_oc_booked_person.toString()
		holder.selfOcBookedBicycle.text = report.self_oc_booked_bicycle.toString()
		holder.selfOcBookedTwoWheeler.text = report.self_oc_booked_two_wheeler.toString()
		holder.selfOcBookedFourWheeler.text = report.self_oc_booked_four_wheeler.toString()
		holder.selfOcBookedOthersCapacity.text = report.self_oc_booked_others_capacity.toString()
		holder.selfPcBookedTicket.text = report.self_pc_booked_ticket.toString()
		holder.selfPcBookedPerson.text = report.self_pc_booked_person.toString()
		holder.selfPcBookedBicycle.text = report.self_pc_booked_bicycle.toString()
		holder.selfPcBookedTwoWheeler.text = report.self_pc_booked_two_wheeler.toString()
		holder.selfPcBookedFourWheeler.text = report.self_pc_booked_four_wheeler.toString()
		holder.selfPcBookedOthersCapacity.text = report.self_pc_booked_others_capacity.toString()
		holder.scannedBookedTicket.text = report.scanned_ticket.toString()
		holder.scannedBookedPerson.text = report.scanned_person.toString()
		holder.scannedBookedBicycle.text = report.scanned_bicycle.toString()
		holder.scannedBookedTwoWheeler.text = report.scanned_two_wheeler.toString()
		holder.scannedBookedFourWheeler.text = report.scanned_four_wheeler.toString()
		holder.scannedBookedOthersCapacity.text = report.scanned_others_capacity.toString()
	}

	override fun getItemCount(): Int {
		return reportList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val ferryName: TextView = itemView.findViewById(R.id.ferry_name)
		val ferryNumber: TextView = itemView.findViewById(R.id.ferry_number)
		val departureTime: TextView = itemView.findViewById(R.id.departure_time)
		val arrivalTime: TextView = itemView.findViewById(R.id.arrival_time)
		val source: TextView = itemView.findViewById(R.id.src)
		val destination: TextView = itemView.findViewById(R.id.dest)
		val availablePerson: TextView = itemView.findViewById(R.id.available_person)
		val availableBicycle: TextView = itemView.findViewById(R.id.available_cycle)
		val availableTwoWheeler: TextView = itemView.findViewById(R.id.available_motorcycle)
		val availableLmv: TextView = itemView.findViewById(R.id.available_lmv)
		val availableHmv: TextView = itemView.findViewById(R.id.available_hmv)
		val availableGoods: TextView = itemView.findViewById(R.id.available_goods)
		val userBookedTicket: TextView = itemView.findViewById(R.id.user_booked_ticket)
		val userBookedPerson: TextView = itemView.findViewById(R.id.user_booked_person)
		val userBookedBicycle: TextView = itemView.findViewById(R.id.user_booked_bicycle)
		val userBookedTwoWheeler: TextView = itemView.findViewById(R.id.user_booked_two_wheeler)
		val userBookedFourWheeler: TextView = itemView.findViewById(R.id.user_booked_four_wheeler)
		val userBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.user_booked_others_capacity)
		val operatorBookedTicket: TextView = itemView.findViewById(R.id.operator_booked_ticket)
		val operatorBookedPerson: TextView = itemView.findViewById(R.id.operator_booked_person)
		val operatorBookedBicycle: TextView = itemView.findViewById(R.id.operator_booked_bicycle)
		val operatorBookedTwoWheeler: TextView =
			itemView.findViewById(R.id.operator_booked_two_wheeler)
		val operatorBookedFourWheeler: TextView =
			itemView.findViewById(R.id.operator_booked_four_wheeler)
		val operatorBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.operator_booked_others_capacity)
		val bookedTicket: TextView = itemView.findViewById(R.id.booked_ticket)
		val bookedPerson: TextView = itemView.findViewById(R.id.booked_person)
		val bookedBicycle: TextView = itemView.findViewById(R.id.booked_bicycle)
		val bookedTwoWheeler: TextView = itemView.findViewById(R.id.booked_two_wheeler)
		val bookedFourWheeler: TextView = itemView.findViewById(R.id.booked_four_wheeler)
		val bookedOthersCapacity: TextView = itemView.findViewById(R.id.booked_others_capacity)
		val selfBookedTicket: TextView = itemView.findViewById(R.id.self_booked_ticket)
		val selfBookedPerson: TextView = itemView.findViewById(R.id.self_booked_person)
		val selfBookedBicycle: TextView = itemView.findViewById(R.id.self_booked_bicycle)
		val selfBookedTwoWheeler: TextView = itemView.findViewById(R.id.self_booked_two_wheeler)
		val selfBookedFourWheeler: TextView = itemView.findViewById(R.id.self_booked_four_wheeler)
		val selfBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.self_booked_others_capacity)
		val selfPcBookedTicket: TextView = itemView.findViewById(R.id.self_pc_booked_ticket)
		val selfPcBookedPerson: TextView = itemView.findViewById(R.id.self_pc_booked_person)
		val selfPcBookedBicycle: TextView = itemView.findViewById(R.id.self_pc_booked_bicycle)
		val selfPcBookedTwoWheeler: TextView =
			itemView.findViewById(R.id.self_pc_booked_two_wheeler)
		val selfPcBookedFourWheeler: TextView =
			itemView.findViewById(R.id.self_pc_booked_four_wheeler)
		val selfPcBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.self_pc_booked_others_capacity)
		val selfOcBookedTicket: TextView = itemView.findViewById(R.id.self_oc_booked_ticket)
		val selfOcBookedPerson: TextView = itemView.findViewById(R.id.self_oc_booked_person)
		val selfOcBookedBicycle: TextView = itemView.findViewById(R.id.self_oc_booked_bicycle)
		val selfOcBookedTwoWheeler: TextView =
			itemView.findViewById(R.id.self_oc_booked_two_wheeler)
		val selfOcBookedFourWheeler: TextView =
			itemView.findViewById(R.id.self_oc_booked_four_wheeler)
		val selfOcBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.self_oc_booked_others_capacity)
		val scannedBookedTicket: TextView = itemView.findViewById(R.id.scanned_booked_ticket)
		val scannedBookedPerson: TextView = itemView.findViewById(R.id.scanned_booked_person)
		val scannedBookedBicycle: TextView = itemView.findViewById(R.id.scanned_booked_bicycle)
		val scannedBookedTwoWheeler: TextView =
			itemView.findViewById(R.id.scanned_booked_two_wheeler)
		val scannedBookedFourWheeler: TextView =
			itemView.findViewById(R.id.scanned_booked_four_wheeler)
		val scannedBookedOthersCapacity: TextView =
			itemView.findViewById(R.id.scanned_booked_others_capacity)
	}
}