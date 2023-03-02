package com.amtron.ferryticket.model

data class PassengerDetails(
	val name: String,
	val phone_number: String,
	val age: String,
	val gender: Gender,
	val p_type: PassengerType,
	val address: String,
	val isDisable: Int
)
