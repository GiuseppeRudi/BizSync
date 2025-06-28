package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.bizsync.domain.constants.enumClass.AbsenceType


data class AbsenceTypeUi(
    val type : AbsenceType,
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val requiresApproval: Boolean
)
