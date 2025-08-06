package com.bizsync.cache.repository

import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnoLocalRepositoryImpl @Inject constructor(
    private val turnoDao: TurnoDao
) : TurnoLocalRepository {


    override suspend fun getTurniByDate(date: LocalDate): Flow<List<Turno>> {
        val turniEntity =  turnoDao.getTurniByDate(date)
        val turni: Flow<List<Turno>> = turniEntity.map { it.toDomainList() }

        return turni
    }


    override suspend fun getTurnoById(turnoId: String): Turno? {
        return turnoDao.getTurnoById(turnoId)?.toDomain()
    }

    override suspend fun exists(turnoId: String): Boolean {
        return turnoDao.exists(turnoId)
    }

    override suspend fun insert(turno: Turno) {
        turnoDao.insert(turno.toEntity())
    }

    override suspend fun update(turno: Turno) {
        turnoDao.update(turno.toEntity())
    }


    override suspend fun getTurni(): List<Turno> {
        return turnoDao.getTurni().toDomainList()
    }

    override suspend fun getTurniInRange(startDate: LocalDate, endDate: LocalDate): List<Turno> {
        return turnoDao.getTurniInRange(startDate, endDate).toDomainList()
    }


    override suspend fun getPastShifts(idAzienda: String, startDate: LocalDate, endDate: LocalDate): List<Turno> {
        return turnoDao.getPastShifts(idAzienda, startDate, endDate).toDomainList()
    }

    override suspend fun getTurniInRangeNonSync(weekStart: LocalDate, weekEnd: LocalDate): List<Turno> {
        return turnoDao.getTurniInRangeNonSync(weekStart, weekEnd).toDomainList()
    }

    override suspend fun getFutureShiftsFromToday(idAzienda: String, fromDate: LocalDate): List<Turno> {
        return turnoDao.getFutureShiftsFromToday(idAzienda, fromDate).toDomainList()
    }

    override suspend fun insertAll(turni: List<Turno>) {
        turnoDao.insertAll(turni.toEntityList())
    }

    override suspend fun clearAll() {
        turnoDao.clearAll()
    }

    override suspend fun getTurniInRangeForUser(startDate: LocalDate, endDate: LocalDate): Flow<List<Turno>> {
        return turnoDao.getTurniInRangeForUser(startDate, endDate).map { entities ->
            entities.toDomainList()
        }
    }


}