package com.bizsync.cache.entity



import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "absences",
    indices = [
        Index(value = ["idAzienda"]),
        Index(value = ["idUser"]),
        Index(value = ["status"]),
        Index(value = ["submittedDate"])
    ]
)
data class AbsenceEntity(
    @PrimaryKey
    val id: String,

    val idUser: String,
    val submittedName: String,
    val idAzienda: String,
    val type: String,

    val startDate: LocalDate,        // ← Direttamente LocalDate
    val endDate: LocalDate,          // ← Direttamente LocalDate
    val submittedDate: LocalDate,    // ← Direttamente LocalDate
    val approvedDate: LocalDate?,    // ← Direttamente LocalDate

    val startTime: LocalTime?,       // ← Direttamente LocalTime
    val endTime: LocalTime?,         // ← Direttamente LocalTime

    val reason: String,
    val status: String,
    val comments: String?,
    val approver: String?,

    val totalDays: Int?,
    val totalHours: Int?,

    val lastUpdated: Long = System.currentTimeMillis()
)