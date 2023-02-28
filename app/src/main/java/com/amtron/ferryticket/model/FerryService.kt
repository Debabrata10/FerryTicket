package com.amtron.ferryticket.model

data class FerryService(
	val id: Int,
	val sc_date: String,
	val ferry_id: Int,
	val s_ghat_id: Int,
	val d_ghat_id: Int,
	val is_private: Int,
	val spot_booking: Int,
	val special_booking: Int,
	val departure_time: String,
	val reached_time: String,
	val passenger_capacity: Int,
	val bicycle_capacity: Int,
	val two_wheeler_capacity: Int,
	val three_wheeler_capacity: Int,
	val four_wheeler: Int,
	val others_capacity: Int,
	val hmv_capacity: Int,
	val ferry: Ferry,
	val source: Ghat,
	val destination: Ghat
)
