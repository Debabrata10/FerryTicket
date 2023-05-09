package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.model.FerryService
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class FerryServiceAdapter(private val ferryServiceList: List<FerryService>) :
	RecyclerView.Adapter<FerryServiceAdapter.ViewHolder>() {
	private lateinit var mItemClickListener: OnRecyclerViewItemClickListener
	private lateinit var ferryService: FerryService

	fun setOnItemClickListener(mItemClickListener: OnRecyclerViewItemClickListener?) {
		this.mItemClickListener = mItemClickListener!!
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.ferry_layout, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		ferryService = ferryServiceList[position]

		holder.ferryName.text = ferryService.ferry.ferry_name
		holder.ferryNumber.text = ferryService.ferry.ferry_no
		holder.refreshBtn.visibility = View.GONE
		holder.departureTime.text =
			DateAndTimeHelper().changeTimeFormat(ferryService.departure_time)
		holder.arrivalTime.text = DateAndTimeHelper().changeTimeFormat(ferryService.reached_time)
		holder.source.text = ferryService.source.ghat_name
		holder.destination.text = ferryService.destination.ghat_name
		holder.availablePerson.text = ferryService.passenger_capacity.toString()
		holder.availableBicycle.text = ferryService.bicycle_capacity.toString()
		holder.availableTwoWheeler.text = ferryService.two_wheeler_capacity.toString()
		holder.availableLmv.text = ferryService.four_wheeler.toString()
		holder.availableHmv.text = ferryService.hmv_capacity.toString()
		holder.availableGoods.text = ferryService.others_capacity.toString()
		if (ferryService.special_booking == 1) {
			holder.specialFerry.visibility = View.VISIBLE
		}
		holder.ferryCard.setOnClickListener {
			mItemClickListener.onItemClickListener(
				position,
				Gson().toJson(ferryServiceList[position])
			)
		}
	}

	override fun getItemCount(): Int {
		return ferryServiceList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val ferryCard: MaterialCardView = itemView.findViewById(R.id.ferry_card)
		val refreshBtn: MaterialButton = itemView.findViewById(R.id.refresh_btn)
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
		val specialFerry: TextView = itemView.findViewById(R.id.special_ferry_txt)
	}
}