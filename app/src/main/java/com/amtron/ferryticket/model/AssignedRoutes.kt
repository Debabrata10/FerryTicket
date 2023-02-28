package com.amtron.ferryticket.model

data class AssignedRoutes(
	val route_id: Int,
	val source_ghat_id: Int,
	val source_ghat_name: String,
	val destination_ghat_id: Int,
	val destination_ghat_name: String,
)
