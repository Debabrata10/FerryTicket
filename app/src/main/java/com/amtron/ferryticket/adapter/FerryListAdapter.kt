package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.helper.DateHelper
import com.amtron.ferryticket.model.Ferry
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class FerryListAdapter (private val ferryList: List<Ferry>) :
    RecyclerView.Adapter<FerryListAdapter.ViewHolder>() {
    private lateinit var mItemClickListener: OnRecyclerViewItemClickListener
    private lateinit var ferry: Ferry

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
        ferry = ferryList[position]

        holder.ferryName.text = ferry.ferry_name
        holder.ferryNumber.text = ferry.ferry_no.toString()
        holder.departure.text = ferry.departure
        holder.source.text = ferry.source_ghat
        holder.destination.text = ferry.destination_ghat
        holder.availablePerson.text = ferry.seat_capacity.toString()
        holder.availableBicycle.text = ferry.bicycle_capacity.toString()
        holder.availableTwoWheeler.text = ferry.two_wheeler.toString()
        holder.availableLmv.text = ferry.four_wheeler_lmv_capacity.toString()
        holder.availableHmv.text = ferry.four_wheeler_hmv_capacity.toString()
        holder.availableGoods.text = ferry.max_load_capacity.toString()
        holder.ferryCard.setOnClickListener {
            mItemClickListener.onItemClickListener(position, Gson().toJson(ferry))
        }
    }

    override fun getItemCount(): Int {
        return ferryList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ferryCard: MaterialCardView = itemView.findViewById(R.id.ferry_card)
        val ferryName: TextView = itemView.findViewById(R.id.ferry_name)
        val ferryNumber: TextView = itemView.findViewById(R.id.ferry_number)
        val departure: TextView = itemView.findViewById(R.id.departure_time)
        val source: TextView = itemView.findViewById(R.id.src)
        val destination: TextView = itemView.findViewById(R.id.dest)
        val availablePerson: TextView = itemView.findViewById(R.id.available_person)
        val availableBicycle: TextView = itemView.findViewById(R.id.available_cycle)
        val availableTwoWheeler: TextView = itemView.findViewById(R.id.available_motorcycle)
        val availableLmv: TextView = itemView.findViewById(R.id.available_lmv)
        val availableHmv: TextView = itemView.findViewById(R.id.available_hmv)
        val availableGoods: TextView = itemView.findViewById(R.id.available_goods)
    }
}