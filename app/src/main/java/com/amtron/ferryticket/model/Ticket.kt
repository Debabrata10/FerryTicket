package com.amtron.ferryticket.model

data class Ticket(
	val ferry_booking_id: Int,
	val ferry_date: String,
	val ferry_name: String,
	val ferry_no: String,
	val s_ghat_name: String,
	val d_ghat_name: String,
	val departure_time: String,
	val reached_time: String,
	val ticket_no: String,
	val passenger_name: String,
	val age: String,
	val gender: String,
	val total_amt: Int,
	val passengers: Int,
	val bicycle_capacity: Int,
	val two_wheeler: Int,
	val four_wheeler: Int,
	val hmv: Int,
	val others_capacity: Int,
)
