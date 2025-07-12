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
    val submittedDate: LocalDate?= null,

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,

    val totalDays: Int? = null,
    val totalHours: Int? = null,

    val reason: String = "",
    val statusUi: AbsenceStatusUi = AbsenceStatus.PENDING.toUiData(),

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

    // Proprietà computed per il display
    val formattedTotalDays: String
        get() = if ( totalDays != null) {
            if (totalDays > 0)
            {
                "$totalDays ${if (totalDays == 1) "giorno" else "giorni"}"
            } else {
                "0 giorni"
            }
        } else {
            "0 giorni"
        }

    val formattedTotalHours: String?
        get() = totalHours?.let { hours ->
            if (hours > 0) {
                "$hours ${if (hours == 1) "ora" else "ore"}"
            } else {
                "0 ore"
            }
        }
}

