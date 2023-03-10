package com.amtron.ferryticket.model

data class PassengerDetails(
	val passenger_name: String,
	val mobile_no: String,
	val age: String,
	val is_ph: Int,
	val address: String,
	val passenger_type: PassengerType,
	val gender: Gender
)
