package com.bizsync.domain.constants.enumClass

// Enum per i tipi di selezione temporale
enum class AbsenceTimeType {
    FULL_DAYS_ONLY,      // Solo giorni interi (VACATION, SICK_LEAVE, STRIKE)
    HOURLY_SINGLE_DAY,   // Solo fascia oraria su singolo giorno (ROL)
    FLEXIBLE             // Scelta tra giorni interi o fascia oraria (PERSONAL_LEAVE, UNPAID_LEAVE)
}