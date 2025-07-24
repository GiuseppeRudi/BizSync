package com.bizsync.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.util.UUID
import java.time.LocalTime

data class AreaLavoro(
    var nomeArea: String = "",

    val orariSettimanali: Map<DayOfWeek, Pair<LocalTime, LocalTime>> = mapOf(
        DayOfWeek.MONDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.TUESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.WEDNESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.THURSDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
        DayOfWeek.FRIDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0))
    )
) {
    constructor() : this("", emptyMap())
}
