package com.amtron.ferryticket.model

data class User(
	val token: String,
	val name: String,
	val last_name: String,
	val email: String,
	val mobile: String,
	val role: String
)