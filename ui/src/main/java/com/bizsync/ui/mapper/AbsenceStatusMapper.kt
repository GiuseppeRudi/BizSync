package com.bizsync.ui.mapper

import androidx.compose.ui.graphics.Color
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.ui.model.AbsenceStatusUi

fun AbsenceStatus.toUiData(): AbsenceStatusUi = when (this) {
    AbsenceStatus.PENDING   -> AbsenceStatusUi(this, "In Attesa",   Color(0xFFFF9800))
    AbsenceStatus.APPROVED  -> AbsenceStatusUi(this, "Approvata",   Color(0xFF4CAF50))
    AbsenceStatus.REJECTED  -> AbsenceStatusUi(this, "Rifiutata",   Color(0xFFF44336))
    AbsenceStatus.CANCELLED -> AbsenceStatusUi(this, "Annullata",   Color(0xFF9E9E9E))
}
