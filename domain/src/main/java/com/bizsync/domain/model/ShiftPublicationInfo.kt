package com.bizsync.domain.model

data class ShiftPublicationInfo(
    val daysUntilShiftPublication: Int,
    val shiftsPublishedThisWeek: Boolean
)