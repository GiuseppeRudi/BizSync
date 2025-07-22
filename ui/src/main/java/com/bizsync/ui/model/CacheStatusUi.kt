package com.bizsync.ui.model

import java.time.LocalDate

data class CacheStatus(
    val hasCurrentData: Boolean = false, // Oggi + 2 settimane
    val hasMonthData: Boolean = false,   // 1 mese
    val hasQuarterData: Boolean = false, // 3 mesi
    val hasYearData: Boolean = false,    // 1 anno
    val lastUpdateDate: LocalDate? = null
)

