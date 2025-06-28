package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import com.bizsync.domain.constants.enumClass.AbsenceStatus


data class AbsenceStatusUi(
    val status: AbsenceStatus,
    val displayName: String,
    val color: Color
)
