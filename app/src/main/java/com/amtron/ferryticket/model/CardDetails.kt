package com.amtron.ferryticket.model

data class CardDetails(
	val id: String,
	val card_id: String,
	val card_no: String,
	var wallet_amount: String,
	val valid_upto: String
)