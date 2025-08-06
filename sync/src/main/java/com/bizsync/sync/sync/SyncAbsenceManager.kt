package com.bizsync.sync.sync

import android.util.Log
import com.bizsync.backend.repository.AbsenceRemoteRepositoryImpl
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.sync.extensions.generateDomainHash
import com.bizsync.sync.storage.AbsenceHashStorage
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncAbsenceManager @Inject constructor(
    private val absenceRemoteRepositoryImpl: AbsenceRemoteRepositoryImpl,
    private val absenceDao: AbsenceDao,
    private val absenceHashStorage: AbsenceHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String, idEmployee: String?): Resource<List<AbsenceEntity>> {
        return try {
            val currentWeekStart = WeeklyWindowCalculator.getCurrentWeekStart()
            val currentWeekKey = WeeklyWindowCalculator.getWeekKey(currentWeekStart)

            val (startDate, endDate) = if (idEmployee == null) {
                WeeklyWindowCalculator.calculateWindowForManager(currentWeekStart)
            } else {
                WeeklyWindowCalculator.calculateWindowForEmployee(currentWeekStart)
            }


            val savedHash = absenceHashStorage.getAbsenceHash(idAzienda, currentWeekKey)

            Log.d("ABSENCE_SYNC", " Chiamata Firebase per assenze azienda $idAzienda (settimana $currentWeekKey)")
            val firebaseResult = absenceRemoteRepositoryImpl.checkAbsenceChangesInWindow(idAzienda, startDate, endDate, idEmployee)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d("ABSENCE_SYNC", " Sync assenze necessario per azienda $idAzienda")
                        Log.d("ABSENCE_SYNC", "   Hash salvato: $savedHash")
                        Log.d("ABSENCE_SYNC", "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash, currentWeekKey,startDate,endDate)
                    } else {
                        Log.d("ABSENCE_SYNC", " CACHE ANCORA VALIDA NON CE DA AGGIORNARE $idAzienda")
                    }

                    val cachedEntities = absenceDao.getAbsences()
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e("ABSENCE_SYNC", " Errore Firebase assenze, uso cache: ${firebaseResult.message}")
                    val cachedEntities = absenceDao.getAbsences()
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    absenceDao.deleteByAziendaInDateRange(idAzienda,startDate.toString(), endDate.toString())
                    absenceHashStorage.saveAbsenceHash(idAzienda, currentWeekKey, "")
                    Log.d("ABSENCE_SYNC", " Nessuna assenza trovata")
                    Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            Log.e("ABSENCE_SYNC", " Errore in syncIfNeeded (assenze): ${e.message}")
            try {
                val cachedEntities = absenceDao.getAbsences()
                Resource.Success(cachedEntities)
            } catch (e: Exception) {
                Resource.Error("Errore sync e cache (assenze): ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String, idEmployee: String?): Resource<Unit> {
        return try {
            absenceHashStorage.deleteAbsenceCache(idAzienda)

            when (val result = syncIfNeeded(idAzienda, idEmployee)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Empty -> Resource.Success(Unit)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore force sync (assenze)")
        }
    }

    private suspend fun performSyncWithData(
        idAzienda: String,
        firebaseData: List<Absence>,
        newHash: String,
        weekKey: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        try {
            val entities = firebaseData.toEntityList()
            absenceDao.deleteByAziendaInDateRange(idAzienda, startDate.toString(), endDate.toString())
            absenceDao.insertAll(entities)

            absenceHashStorage.saveAbsenceHash(idAzienda, weekKey, newHash)

            Log.d("ABSENCE_SYNC", "Sync assenze completato - ${entities.size} assenze - hash: $newHash")

        } catch (e: Exception) {
            Log.e("ABSENCE_SYNC", " Errore durante sync assenze: ${e.message}")
            throw e
        }
    }


}