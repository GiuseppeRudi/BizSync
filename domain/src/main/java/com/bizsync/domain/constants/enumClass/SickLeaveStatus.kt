package com.bizsync.domain.constants.enumClass

enum class SickLeaveStatus {
    PENDING_VERIFICATION,      // In attesa di verifica sistema
    REQUIRES_SHIFT_MANAGEMENT, // Richiede gestione turni
    VERIFIED_NO_SHIFTS        // Verificata senza turni coinvolti
}