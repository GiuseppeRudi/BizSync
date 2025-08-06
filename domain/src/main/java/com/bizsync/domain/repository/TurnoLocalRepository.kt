package com.bizsync.domain.repository

import com.bizsync.domain.model.Turno
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TurnoLocalRepository {
    suspend fun getTurniByDate(date: LocalDate): Flow<List<Turno>>
    suspend fun clearAll()

    suspend fun getPastShifts(idAzienda: String, startDate: LocalDate, endDate: LocalDate): List<Turno>
    suspend fun getFutureShiftsFromToday(idAzienda: String, today: LocalDate): List<Turno>
    suspend fun getTurniInRangeNonSync(weekStart: LocalDate, weekEnd: LocalDate): List<Turno>
    suspend fun insertAll(turni: List<Turno>)

    suspend fun getTurni(): List<Turno>
    suspend fun getTurniInRange(startDate: LocalDate, endDate: LocalDate): List<Turno>

    suspend fun getTurniInRangeForUser(startDate: LocalDate, endDate: LocalDate): Flow<List<Turno>>

    suspend fun getTurnoById(turnoId: String): Turno?
    suspend fun exists(turnoId: String): Boolean
    suspend fun insert(turno: Turno)
    suspend fun update(turno: Turno)

}
