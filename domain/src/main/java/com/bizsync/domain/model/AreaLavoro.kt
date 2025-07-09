package com.bizsync.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.util.UUID

@Serializable
data class AreaLavoro(
    val id: String = UUID.randomUUID().toString(),
    var nomeArea: String = "",

    val orariSettimanali: Map<DayOfWeek, Pair<String, String>> = mapOf(
        DayOfWeek.MONDAY to ("08:00" to "18:00"),
        DayOfWeek.TUESDAY to ("08:00" to "18:00"),
        DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
        DayOfWeek.THURSDAY to ("08:00" to "18:00"),
        DayOfWeek.FRIDAY to ("08:00" to "18:00")
    )
)
{
    constructor() : this(UUID.randomUUID().toString(),"")
}