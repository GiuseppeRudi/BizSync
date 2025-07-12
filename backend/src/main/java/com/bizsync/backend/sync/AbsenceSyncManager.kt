package com.bizsync.backend.sync

import android.util.Log
import com.bizsync.backend.hash.extensions.generateDomainHash
import com.bizsync.backend.hash.storage.AbsenceHashStorage
import com.bizsync.domain.utils.AbsenceWindowCalculator
import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsenceSyncManager @Inject constructor(
    private val absenceRepository: AbsenceRepository,
    private val absenceDao: AbsenceDao,
    private val absenceHashStorage: AbsenceHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String): Resource<List<AbsenceEntity>> {
        return try {
            val currentWeekStart = AbsenceWindowCalculator.getCurrentWeekStart()
            val currentWeekKey = AbsenceWindowCalculator.getWeekKey(currentWeekStart)
            val (startDate, endDate) = AbsenceWindowCalculator.calculateAbsenceWindow(currentWeekStart)

            val savedHash = absenceHashStorage.getAbsenceHash(idAzienda, currentWeekKey)

            Log.d("ABSENCE_SYNC", "🌐 Chiamata Firebase per assenze azienda $idAzienda (settimana $currentWeekKey)")
            val firebaseResult = absenceRepository.checkAbsenceChangesInWindow(idAzienda, startDate, endDate)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d("ABSENCE_SYNC", "🔄 Sync assenze necessario per azienda $idAzienda")
                        Log.d("ABSENCE_SYNC", "   Hash salvato: $savedHash")
                        Log.d("ABSENCE_SYNC", "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash, currentWeekKey)
                    } else {
                        Log.d("ABSENCE_SYNC", "✅ CACHE ANCORA VALIDA NON CE DA AGGIORNARE $idAzienda")
                    }

                    val cachedEntities = absenceDao.getAbsencesByAzienda(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e("ABSENCE_SYNC", "❌ Errore Firebase assenze, uso cache: ${firebaseResult.message}")
                    val cachedEntities = absenceDao.getAbsencesByAzienda(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    absenceDao.deleteByAzienda(idAzienda)
                    absenceHashStorage.saveAbsenceHash(idAzienda, currentWeekKey, "")
                    Log.d("ABSENCE_SYNC", "📭 Nessuna assenza trovata")
                    Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            Log.e("ABSENCE_SYNC", "🚨 Errore in syncIfNeeded (assenze): ${e.message}")
            try {
                val cachedEntities = absenceDao.getAbsencesByAzienda(idAzienda)
                Resource.Success(cachedEntities)
            } catch (cacheError: Exception) {
                Resource.Error("Errore sync e cache (assenze): ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return try {
            absenceHashStorage.deleteAbsenceCache(idAzienda)

            when (val result = syncIfNeeded(idAzienda)) {
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
        weekKey: String
    ) {
        try {
            val entities = firebaseData.toEntityList()
            absenceDao.deleteByAzienda(idAzienda)
            absenceDao.insertAll(entities)

            absenceHashStorage.saveAbsenceHash(idAzienda, weekKey, newHash)

            Log.d("ABSENCE_SYNC", "✅ Sync assenze completato - ${entities.size} assenze - hash: $newHash")

        } catch (e: Exception) {
            Log.e("ABSENCE_SYNC", "❌ Errore durante sync assenze: ${e.message}")
            throw e
        }
    }

    suspend fun validateCacheIntegrity(idAzienda: String): Boolean {
        return try {
            val currentWeekStart = AbsenceWindowCalculator.getCurrentWeekStart()
            val currentWeekKey = AbsenceWindowCalculator.getWeekKey(currentWeekStart)

            val savedHash = absenceHashStorage.getAbsenceHash(idAzienda, currentWeekKey) ?: return false
            val cachedData = absenceDao.getAbsencesByAzienda(idAzienda)
            val currentCacheHash = cachedData.toDomainList().generateDomainHash()

            val isValid = savedHash == currentCacheHash

            if (!isValid) {
                Log.w("ABSENCE_SYNC", "⚠️ Cache assenze non valida per azienda $idAzienda")
                Log.w("ABSENCE_SYNC", "   Hash salvato: $savedHash")
                Log.w("ABSENCE_SYNC", "   Hash cache: $currentCacheHash")
            }

            isValid
        } catch (e: Exception) {
            false
        }
    }
}