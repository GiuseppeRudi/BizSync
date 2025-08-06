package com.bizsync.domain.repository

import com.bizsync.domain.model.Timbratura
import kotlinx.coroutines.flow.Flow

interface TimbraturaLocalRepository {
    suspend fun getTimbratureByDate(startDate: String, endDate: String): Flow<List<Timbratura>>
    suspend fun getRecentTimbrature(limit: Int): Flow<List<Timbratura>>
    suspend fun getByTurnoAndDipendente(turnoId: String, dipendenteId: String): List<Timbratura>
    suspend fun clearAll()

    suspend fun getTimbratureInRangeForUser(startDate: java.time.LocalDate, endDate: java.time.LocalDate, userId: String): Flow<List<Timbratura>>

}