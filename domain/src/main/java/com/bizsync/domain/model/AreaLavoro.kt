package com.bizsync.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.util.UUID
import java.time.LocalTime

// @Serializable
data class AreaLavoro(
    val id: String = UUID.randomUUID().toString(),
    var nomeArea: String = "",

    // Conversione in LocalTime
    val orariSettimanali: Map<DayOfWeek, Pair<LocalTime, LocalTime>> = mapOf(
        DayOfWeek.MONDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.TUESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.WEDNESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.THURSDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.FRIDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0))
    )
)
{
    constructor() : this(UUID.randomUUID().toString(),"")
}