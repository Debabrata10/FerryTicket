package com.amtron.ferryticket.model

data class MasterData(
	val passengerTypes: ArrayList<PassengerType>,
	val vehicleTypes: ArrayList<VehicleType>,
	val otherTypes: ArrayList<OtherType>,
	val genders: ArrayList<Gender>
)
