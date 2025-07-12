package com.bizsync.cache.entity



import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 1. ABSENCE ENTITY per Room Database
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
    val type: String, // AbsenceType as String

    // Date fields as String for Room compatibility
    val startDate: String, // LocalDate.toString()
    val endDate: String,   // LocalDate.toString()
    val submittedDate: String, // LocalDate.toString()
    val approvedDate: String?, // LocalDate?.toString()

    // Time fields as String (nullable for full-day absences)
    val startTime: String?, // LocalTime?.toString()
    val endTime: String?,   // LocalTime?.toString()

    val reason: String,
    val status: String, // AbsenceStatus as String
    val comments: String?,
    val approver: String?,

    // Duration fields
    val totalDays: Int?,
    val totalHours: Int?,

    // Sync field
    val lastUpdated: Long = System.currentTimeMillis() // Timestamp for sync
)

