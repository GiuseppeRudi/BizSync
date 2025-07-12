package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import java.time.LocalDate
import java.time.LocalTime


data class Absence(
    val id: String,
    val idUser: String,
    val submittedName: String,
    val idAzienda : String,
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val reason: String,
    val status: AbsenceStatus,
    val submittedDate: LocalDate,
    val approvedBy: String? = null,
    val approvedDate: LocalDate? = null,
    val comments: String? = null,
    val totalDays: Int? = null,
    val totalHours: Int? = null,
)
