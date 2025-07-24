package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.bizsync.domain.constants.enumClass.AbsenceTimeType
import com.bizsync.domain.constants.enumClass.AbsenceType


data class AbsenceTypeUi(
    val type : AbsenceType,
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val requiresApproval: Boolean
)

fun AbsenceType.getTimeType(): AbsenceTimeType {
    return when (this) {
        AbsenceType.VACATION,
        AbsenceType.SICK_LEAVE,
        AbsenceType.STRIKE -> AbsenceTimeType.FULL_DAYS_ONLY

        AbsenceType.ROL -> AbsenceTimeType.HOURLY_SINGLE_DAY

        AbsenceType.PERSONAL_LEAVE,
        AbsenceType.UNPAID_LEAVE -> AbsenceTimeType.FLEXIBLE
    }
}