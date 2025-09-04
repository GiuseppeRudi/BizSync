package com.bizsync.cache.repository

import android.util.Log
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimbraturaLocalRepositoryImpl @Inject constructor(
    private val timbraturaDao: TimbraturaDao
) : TimbraturaLocalRepository {

    override suspend fun getTimbratureByDate(startDate: String, endDate: String): Flow<List<Timbratura>> {
        Log.d("TimbratureRepo", "getTimbratureByDate called with startDate: $startDate, endDate: $endDate")

        return timbraturaDao.getTimbratureByDate(startDate, endDate).map { entities ->
            Log.d("TimbratureRepo", "Raw entities from DAO: ${entities.size} items")
            entities.forEachIndexed { index, entity ->
                Log.v("TimbratureRepo", "Entity $index: id=${entity.id}, date=${entity.dataOraTimbratura}, type=${entity.dataOraTimbratura}")
            }

            val domainObjects = entities.map { it.toDomain() }
            Log.d("TimbratureRepo", "Converted to domain objects: ${domainObjects.size} items")

            domainObjects.forEach { timbratura ->
                Log.v("TimbratureRepo", "Domain object: ${timbratura}")
            }

            Log.i("TimbratureRepo", "Successfully returning ${domainObjects.size} timbrature for period $startDate to $endDate")
            domainObjects
        }.catch { exception ->
            Log.e("TimbratureRepo", "Error in getTimbratureByDate: ${exception.message}", exception)
            throw exception
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
