package com.bizsync.cache.entity



import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

    val startDate: String,
    val endDate: String,
    val submittedDate: String,
    val approvedDate: String?,

    val startTime: String?,
    val endTime: String?,

    val reason: String,
    val status: String,
    val comments: String?,
    val approver: String?,


    val totalDays: Int?,
    val totalHours: Int?,

    val lastUpdated: Long = System.currentTimeMillis()
)

