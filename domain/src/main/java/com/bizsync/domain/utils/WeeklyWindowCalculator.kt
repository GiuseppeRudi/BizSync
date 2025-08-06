package com.bizsync.domain.utils

import java.time.DayOfWeek
import java.time.LocalDate

// 2. UTILITY per calcolo finestre settimanali
object WeeklyWindowCalculator {

    fun getCurrentWeekStart(): LocalDate {
        return LocalDate.now().with(DayOfWeek.MONDAY)
    }

    fun getWeekStartFromDate(selectionDate: LocalDate): LocalDate {
        return selectionDate.with(DayOfWeek.MONDAY)
    }

    fun calculateWindowForEmployee(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        val startDate = weekStart.minusWeeks(4)
        val endDate = weekStart.plusWeeks(1).with(DayOfWeek.SUNDAY)
        return Pair(startDate, endDate)
    }


    fun calculateWindowForManager(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        val startDate = weekStart.minusWeeks(2)
        val endDate = weekStart.plusWeeks(1).with(DayOfWeek.SUNDAY)
        return Pair(startDate, endDate)
    }


    fun getWeekKey(weekStart: LocalDate): String {
        return weekStart.toString() // es. "2025-07-07"
    }

    fun getWeekBounds(date: LocalDate): Pair<LocalDate, LocalDate> {
        val monday = date.with(DayOfWeek.MONDAY)
        val sunday = date.with(DayOfWeek.SUNDAY)
        return monday to sunday
    }

}