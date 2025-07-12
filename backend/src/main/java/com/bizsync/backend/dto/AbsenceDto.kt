package com.bizsync.backend.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

// 1. PRIMO PASSO: Aggiungi lastUpdated al DTO Firebase
data class AbsenceDto(
    @get:Exclude
    val id: String = "",

    val submittedName: String = "",
    val idUser: String = "",
    val idAzienda: String = "",
    val type: String = "",
    val startDateTime: Timestamp? = null,
    val endDateTime: Timestamp? = null,
    val reason: String = "",
    val status: String = "",
    val submittedDate: Timestamp? = null,
    val approvedBy: String? = null,
    val approvedDate: Timestamp? = null,
    val comments: String? = null,
    val totalDays: Int? = null,
    val totalHours: Int? = null,

)
