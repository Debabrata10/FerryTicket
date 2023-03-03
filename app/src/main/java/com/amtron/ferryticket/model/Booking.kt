package com.amtron.ferryticket.model

data class Booking(
	val passengers: List<PassengerDetails>,
	val vehicles: List<Vehicle>,
	val others: List<Others>
)
