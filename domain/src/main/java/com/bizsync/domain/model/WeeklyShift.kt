package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class WeeklyShift(
    val id: String = "",
    val idAzienda: String,
    val weekStart: LocalDate,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val status: WeeklyShiftStatus,
    val dipartimentiAttivi: List<AreaLavoro>,
    val dipendentiAttivi: List<User>

    )