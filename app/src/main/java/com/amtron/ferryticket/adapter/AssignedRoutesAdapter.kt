package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.AssignedRoutes
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class AssignedRoutesAdapter(
	private val assignedRoutesList: List<AssignedRoutes>
) :
	RecyclerView.Adapter<AssignedRoutesAdapter.ViewHolder>() {
	private lateinit var mItemClickListener: OnAssignedRoutesRecyclerViewItemClickListener
	private lateinit var assignedRoutes: AssignedRoutes

	fun setOnItemClickListener(mItemClickListener: OnAssignedRoutesRecyclerViewItemClickListener?) {
		this.mItemClickListener = mItemClickListener!!
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.sorce_destination_card_view, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		assignedRoutes = assignedRoutesList[position]

		holder.srcGhat.text = assignedRoutes.source_ghat_name
		holder.destGhat.text = assignedRoutes.destination_ghat_name
		holder.searchFerries.setOnClickListener {
			mItemClickListener.onAssignedRoutesItemClickListener(
				position,
				Gson().toJson(assignedRoutesList[position]),
				"getFerries"
			)
		}
		holder.getReport.setOnClickListener {
			mItemClickListener.onAssignedRoutesItemClickListener(
				position,
				Gson().toJson(assignedRoutesList[position]),
				"getReport"
			)
		}
	}

	override fun getItemCount(): Int {
		return assignedRoutesList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val searchFerries: MaterialButton = itemView.findViewById(R.id.search_ferries_btn)
		val getReport: MaterialButton = itemView.findViewById(R.id.get_report_btn)
		val srcGhat: TextView = itemView.findViewById(R.id.src)
		val destGhat: TextView = itemView.findViewById(R.id.dest)
	}
}