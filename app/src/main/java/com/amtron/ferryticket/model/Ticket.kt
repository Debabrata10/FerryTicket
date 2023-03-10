package com.amtron.ferryticket.model

data class Ticket(
	val id: Int,
	val passenger_id: Int? = null,
	val ticket_no: String,
	val ferry_service_id: Int,
	val two_way: Int,
	val ferry_date: String,
	val passengers: Int,
	val bicycle_capacity: Int,
	val two_wheeler: Int,
	val three_wheeler: Int,
	val four_wheeler: Int,
	val hmv: Int,
	val others_capacity: Int,
	val net_amt: Double,
	val service_amt: Double,
	val total_amt: Double,
	val mode_of_payment: String,
	val order_status: String,
	val scanned: Int,
	val ferry_payment_detail_id: Int? = null,
	val is_private: Int,
	val is_lease: Int,
	val passenger: List<PassengerDetails>,
	val vehicle: List<Vehicle>,
	val other: List<Others>
)
