package com.amtron.ferryticket.model

data class Booking(
	val ferry_service_id: Int,
	val passenger_data: List<PassengerDetails>,
	val vehicle_data: List<Vehicle>,
	val other_data: List<Others>
)
