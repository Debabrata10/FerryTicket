package com.amtron.ferryticket.adapter

import com.amtron.ferryticket.model.Ticket

interface OnRecyclerViewItemClickListener {
	fun onItemClickListener(position: Int, type: String)
}