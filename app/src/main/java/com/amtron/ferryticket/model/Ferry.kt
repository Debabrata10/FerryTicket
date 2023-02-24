package com.amtron.ferryticket.model

data class Ferry(
    val id: Int,
    var ferry_name: String,
    var ferry_no: Int,
    var ferry_type: String,
    var source_ghat: String,
    var destination_ghat: String,
    var departure: String,
    var arrival: String,
    var seat_capacity: Int,
    var bicycle_capacity: Int,
    var two_wheeler: Int,
    var four_wheeler_lmv_capacity: Int,
    var four_wheeler_hmv_capacity: Int,
    var max_load_capacity: Int
)
