package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import java.time.LocalDate

interface WeeklyShiftRepository {
    suspend fun getThisWeekPublishedShift(idAzienda: String, weekStart: LocalDate): Resource<Any?>
    suspend fun getWeeklyShiftCorrente(weekStart: LocalDate): Resource<WeeklyShift?>
    suspend fun getWeeklyShift(idAzienda: String, weekStart: LocalDate): Resource<WeeklyShift?>
    suspend fun createWeeklyShift(weeklyShift: WeeklyShift): Resource<String>
    suspend fun updateWeeklyShiftStatus(weeklyShift: WeeklyShift): Resource<Unit>
}