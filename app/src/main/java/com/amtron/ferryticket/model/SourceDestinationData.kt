package com.amtron.ferryticket.model

data class SourceDestinationData(
    val src_dest_id: Int,
    val source: Ghat,
    val destination: Ghat,
    val timing: String
)