package com.bizsync.domain.model

data class SaveAbsenceResult(
    val savedAbsence: Absence,
    val updatedContract: Contratto
)