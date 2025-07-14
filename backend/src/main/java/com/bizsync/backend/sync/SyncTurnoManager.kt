package com.bizsync.backend.sync

import com.bizsync.backend.hash.storage.TurniHashStorage


import android.util.Log
import com.bizsync.backend.hash.extensions.generateCacheHash
import com.bizsync.backend.hash.extensions.generateDomainHash
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.AbsenceWindowCalculator
import javax.inject.Inject

class SyncTurnoManager @Inject constructor(
    private val turnoRepository: TurnoRepository,
    private val turnoDao: TurnoDao,
    private val hashStorage: TurniHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String): Resource<List<TurnoEntity>> {
        return try {
            val currentWeekStart = AbsenceWindowCalculator.getCurrentWeekStart()
            val currentWeekKey = AbsenceWindowCalculator.getWeekKey(currentWeekStart)
            val (startDate, endDate) = AbsenceWindowCalculator.calculateAbsenceWindow(currentWeekStart)


            val savedHash = hashStorage.getTurniHash(idAzienda)

            Log.d("TURNI_DEBUG", "🌐 Chiamata Firebase per turni azienda $idAzienda")
            val firebaseResult = turnoRepository.getTurniRangeByAzienda(idAzienda,startDate, endDate)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d("TURNI_DEBUG", "🔄 Sync turni necessario per azienda $idAzienda")
                        Log.d("TURNI_DEBUG", "   Hash salvato: $savedHash")
                        Log.d("TURNI_DEBUG", "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash)
                    } else {
                        Log.d("TURNI_DEBUG", "✅ Cache valida - nessun aggiornamento necessario")
                    }

                    val cachedEntities = turnoDao.getTurniByAzienda(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e("TURNI_DEBUG", "❌ Errore Firebase turni, uso cache: ${firebaseResult.message}")
                    val cachedEntities = turnoDao.getTurniByAzienda(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    turnoDao.deleteByAzienda(idAzienda)
                    hashStorage.saveTurniHash(idAzienda, "")
                    Resource.Success(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "🚨 Errore in syncIfNeeded (turni): ${e.message}")
            try {
                val cachedEntities = turnoDao.getTurniByAzienda(idAzienda)
                Resource.Success(cachedEntities)
            } catch (cacheError: Exception) {
                Resource.Error("Errore sia in sync che nella cache (turni): ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return try {
            hashStorage.deleteTurniHash(idAzienda)

            when (val result = syncIfNeeded(idAzienda)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Empty -> Resource.Success(Unit)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore force sync (turni)")
        }
    }

    private suspend fun performSyncWithData(
        idAzienda: String,
        firebaseData: List<Turno>,
        newHash: String
    ) {
        try {
            val entities = firebaseData.toEntityList()
            turnoDao.deleteByAzienda(idAzienda)
            turnoDao.insertAll(entities)
            hashStorage.saveTurniHash(idAzienda, newHash)

            Log.d("TURNI_DEBUG", "✅ Sync turni completato - ${entities.size} turni - hash: $newHash")
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "❌ Errore durante il sync turni: ${e.message}")
            throw e
        }
    }

    suspend fun validateCacheIntegrity(idAzienda: String): Boolean {
        return try {
            val savedHash = hashStorage.getTurniHash(idAzienda) ?: return false
            val cachedData = turnoDao.getTurniByAzienda(idAzienda)
            val currentCacheHash = cachedData.generateCacheHash()

            val isValid = savedHash == currentCacheHash
            if (!isValid) {
                Log.w("TURNI_DEBUG", "⚠️ Cache turni non valida per azienda $idAzienda")
                Log.w("TURNI_DEBUG", "   Hash salvato: $savedHash")
                Log.w("TURNI_DEBUG", "   Hash attuale: $currentCacheHash")
            }

            isValid
        } catch (e: Exception) {
            false
        }
    }
}
