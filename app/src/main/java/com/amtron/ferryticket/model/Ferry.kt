package com.amtron.ferryticket.model

data class Ferry(
	val id: Int,
	var ferry_name: String,
	var ferry_no: String,
	var ferry_type_id: String,
	var source: Ghat,
	var destination: Ghat,
	var departure: String,
	var arrival: String,
	var seat_capacity: String,
	var tatkal_capacity: Int,
	var bicycle_capacity: String,
	var two_wheeler: String,
	var three_wheeler: Int,
	var four_wheeler_lmv_capacity: String,
	var four_wheeler_hmv_capacity: String,
	var others_capacity: Int,
	var is_private: Int,
	var extra_passenger_capacity: Int,
	var extra_lmv_capacity: Int,
	var extra_two_wheeler_capacity: Int,
	var max_load_capacity: String
)
