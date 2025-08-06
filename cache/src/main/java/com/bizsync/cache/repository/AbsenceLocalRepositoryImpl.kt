package com.bizsync.cache.repository

import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsenceLocalRepositoryImpl @Inject constructor(
    private val absenceDao: AbsenceDao
) : AbsenceLocalRepository {


    override suspend fun insertAll(absences: List<Absence>) {
        absenceDao.insertAll(absences.toEntityList())
    }

    override suspend fun getAbsences(): List<Absence> {
        return absenceDao.getAbsences().toDomainList()
    }

    override suspend fun getAbsencesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Absence>> {
        return absenceDao.getAbsencesInRange(startDate, endDate).map { entities ->
            entities.toDomainList()
        }
    }

    override suspend fun clearAll() {
        absenceDao.clearAll()
    }

    override suspend fun getAbsencesByUser(userId: String): List<Absence> {
        return absenceDao.getAbsencesByUser(userId).toDomainList()
    }

}
