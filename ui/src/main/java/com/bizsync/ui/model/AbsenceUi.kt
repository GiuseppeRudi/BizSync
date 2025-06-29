package com.bizsync.ui.model

import java.time.LocalDate

import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.ui.mapper.toUiData
import java.time.LocalTime

data class AbsenceUi(
    val id: String = "",
    val idUser: String = "",
    val idAzienda : String = "",

    val typeUi: AbsenceTypeUi = AbsenceType.VACATION.toUiData(),

    val submittedName: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,

    val totalDays: String = "0 giorni",
    val reason: String = "",
    val statusUi: AbsenceStatusUi = AbsenceStatus.PENDING.toUiData(),
    val submittedDate: LocalDate?= null,
    val comments: String? = null,
    val approver: String? = null,
    val approvedDate: LocalDate? = null
) {
    val formattedDateRange: String
        get() = if (startDate != null && endDate != null) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        } else {
            "Data non disponibile"
        }

    val formattedHours: String?
        get() = if (startTime != null && endTime != null) {
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
        } else null
}

