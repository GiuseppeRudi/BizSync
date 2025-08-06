package com.bizsync.cache.repository

import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimbraturaLocalRepositoryImpl @Inject constructor(
    private val timbraturaDao: TimbraturaDao
) : TimbraturaLocalRepository {

    override suspend fun getTimbratureByDate(startDate: String, endDate: String): Flow<List<Timbratura>> {
        return timbraturaDao.getTimbratureByDate(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecentTimbrature(limit: Int): Flow<List<Timbratura>> {
        return timbraturaDao.getRecentTimbrature(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getByTurnoAndDipendente(turnoId: String, dipendenteId: String): List<Timbratura> {
        return timbraturaDao.getByTurnoAndDipendente(turnoId, dipendenteId).map { it.toDomain() }
    }

    override suspend fun clearAll() {
        timbraturaDao.clearAll()
    }

    override suspend fun getTimbratureInRangeForUser(startDate: LocalDate, endDate: LocalDate, userId: String): Flow<List<Timbratura>> {
        return timbraturaDao.getTimbratureInRangeForUser(startDate, endDate, userId).map { entities ->
            entities.toDomainList()
        }
    }
}
