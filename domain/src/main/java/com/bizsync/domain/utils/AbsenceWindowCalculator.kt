package com.bizsync.domain.utils

import java.time.DayOfWeek
import java.time.LocalDate

// 2. UTILITY per calcolo finestre settimanali
object AbsenceWindowCalculator {

    fun getCurrentWeekStart(): LocalDate {
        return LocalDate.now().with(DayOfWeek.MONDAY)
    }

    fun calculateAbsenceWindow(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        // Finestra: 2 settimane indietro + 2 settimane avanti
        val startDate = weekStart.minusWeeks(2)
        val endDate = weekStart.plusWeeks(1).with(DayOfWeek.SUNDAY)
        return Pair(startDate, endDate)
    }

    fun getWeekKey(weekStart: LocalDate): String {
        return weekStart.toString() // es. "2025-07-07"
    }
}