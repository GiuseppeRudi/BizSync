package com.bizsync.sync.sync


import android.util.Log
import com.bizsync.backend.repository.TurnoRemoteRepositoryImpl
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.sync.extensions.generateDomainHash
import com.bizsync.sync.storage.TurniHashStorage
import java.time.LocalDate
import javax.inject.Inject

class SyncTurnoManager @Inject constructor(
    private val turnoRemoteRepositoryImpl: TurnoRemoteRepositoryImpl,
    private val turnoDao: TurnoDao,
    private val hashStorage: TurniHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String, idEmployee : String?): Resource<List<TurnoEntity>> {
        return try {
            val currentWeekStart = WeeklyWindowCalculator.getCurrentWeekStart()

            val (startDate, endDate) = if (idEmployee == null) {
                WeeklyWindowCalculator.calculateWindowForManager(currentWeekStart)
            } else {
                WeeklyWindowCalculator.calculateWindowForEmployee(currentWeekStart)
            }

            val savedHash = hashStorage.getTurniHash(idAzienda)

            Log.d("TURNI_DEBUG", " Chiamata Firebase per turni azienda $idAzienda")
            val firebaseResult = turnoRemoteRepositoryImpl.getTurniRangeByAzienda(idAzienda,startDate, endDate, idEmployee)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d("TURNI_DEBUG", " Sync turni necessario per azienda $idAzienda")
                        Log.d("TURNI_DEBUG", "   Hash salvato: $savedHash")
                        Log.d("TURNI_DEBUG", "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash,startDate,endDate)
                    } else {
                        Log.d("TURNI_DEBUG", " Cache valida - nessun aggiornamento necessario")
                    }

                    val cachedEntities = turnoDao.getTurni()
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e("TURNI_DEBUG", " Errore Firebase turni, uso cache: ${firebaseResult.message}")
                    val cachedEntities = turnoDao.getTurni()
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    turnoDao.deleteByAziendaForManager(idAzienda, startDate, endDate)
                    hashStorage.saveTurniHash(idAzienda, "")
                    Resource.Success(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", " Errore in syncIfNeeded (turni): ${e.message}")
            try {
                val cachedEntities = turnoDao.getTurni()
                Resource.Success(cachedEntities)
            } catch (e: Exception) {
                Resource.Error("Errore sia in sync che nella cache (turni): ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String, idEmployee : String?): Resource<Unit> {
        return try {
            hashStorage.deleteTurniHash(idAzienda)

            when (val result = syncIfNeeded(idAzienda,idEmployee)) {
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
        newHash: String,
        startDate : LocalDate,
        endDate : LocalDate
    ) {
        try {
            val entities = firebaseData.toEntityList().map { it.copy(isSynced = true) }
            turnoDao.deleteByAziendaForManager(idAzienda,startDate,endDate)
            turnoDao.insertAll(entities)
            hashStorage.saveTurniHash(idAzienda, newHash)

            Log.d("TURNI_DEBUG", " Sync turni completato - ${entities.size} turni - hash: $newHash")
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", " Errore durante il sync turni: ${e.message}")
            throw e
        }
    }


}
