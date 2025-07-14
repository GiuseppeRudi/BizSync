package com.bizsync.backend.orchestrator


import android.util.Log
import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.backend.sync.SyncTurnoManager
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import javax.inject.Inject

class TurnoOrchestrator @Inject constructor(
    private val turnoRepository: TurnoRepository,
    private val turnoDao: TurnoDao,
    private val hashStorage: HashStorage,
    private val syncTurnoManager: SyncTurnoManager
) {

    suspend fun updateTurno(turno: Turno): Resource<String> {
        return try {
            Log.d("TURNO_UPDATE", "🔄 Aggiornamento turno ID: ${turno.id}")

            // 1. Aggiorna SOLO Firebase
            when (val firebaseResult = turnoRepository.updateTurno(turno)) {
                is Resource.Success -> {
                    Log.d("TURNO_UPDATE", "✅ Firebase aggiornato, ora forzo sync")

                    // 2. Forza sync completo che aggiorna automaticamente DAO + Hash
                    when (getTurni(turno.dipartimentoId, forceRefresh = true)) {
                        is Resource.Success -> {
                            Log.d("TURNO_UPDATE", "✅ Sync completato, tutto allineato!")
                            Resource.Success(firebaseResult.data)
                        }
                        is Resource.Error -> {
                            Log.w("TURNO_UPDATE", "⚠️ Sync fallito ma Firebase è aggiornato")
                            Resource.Success(firebaseResult.data)
                        }
                        else -> Resource.Success(firebaseResult.data)
                    }
                }

                is Resource.Error -> {
                    Log.e("TURNO_UPDATE", "❌ Errore Firebase: ${firebaseResult.message}")
                    Resource.Error(firebaseResult.message)
                }

                else -> {
                    Resource.Error("Errore imprevisto nell'aggiornamento")
                }
            }
        } catch (e: Exception) {
            Log.e("TURNO_UPDATE", "🚨 Errore imprevisto: ${e.message}")
            Resource.Error(e.message ?: "Errore nell'aggiornamento turno")
        }
    }

    suspend fun getTurni(idAzienda: String, forceRefresh: Boolean = false): Resource<List<Turno>> {
        return try {
            if (forceRefresh) {
                Log.d("TURNI_DEBUG", "🔄 Force sync attivato per azienda $idAzienda")

                val syncResult = syncTurnoManager.forceSync(idAzienda)
                if (syncResult is Resource.Error) {
                    Log.e("TURNI_DEBUG", "❌ Errore durante forceSync: ${syncResult.message}")
                }

                val cachedEntities = turnoDao.getTurniByAzienda(idAzienda)
                Log.d("TURNI_DEBUG", "📦 Recuperati ${cachedEntities.size} turni dalla cache dopo forceSync")
                Resource.Success(cachedEntities.toDomainList())
            } else {
                Log.d("TURNI_DEBUG", "⚙️ Sync intelligente per azienda $idAzienda")

                when (val result = syncTurnoManager.syncIfNeeded(idAzienda)) {
                    is Resource.Success -> {
                        val domainTurni = turnoDao.getTurniByAzienda(idAzienda).toDomainList()
                        Log.d("TURNI_DEBUG", "✅ Sync completato, turni recuperati dalla cache: ${domainTurni.size}")
                        Resource.Success(domainTurni)
                    }

                    is Resource.Error -> {
                        Log.e("TURNI_DEBUG", "❌ Errore sync intelligente: ${result.message}")
                        Resource.Error(result.message)
                    }

                    is Resource.Empty -> {
                        Log.d("TURNI_DEBUG", "📭 Nessun turno disponibile da Firebase")
                        Resource.Success(emptyList())
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "🚨 Errore imprevisto nel recupero turni: ${e.message}")
            Resource.Error(e.message ?: "Errore nel recupero turni")
        }
    }
}
