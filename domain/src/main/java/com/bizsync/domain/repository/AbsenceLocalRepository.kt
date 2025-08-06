package com.bizsync.domain.repository

import com.bizsync.domain.model.Absence
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AbsenceLocalRepository {

    suspend fun getAbsencesByUser(userId: String): List<Absence>

    suspend fun insertAll(absences: List<Absence>)
    suspend fun clearAll()

    suspend fun getAbsences(): List<Absence>
    suspend fun getAbsencesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Absence>>

}