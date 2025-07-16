package com.bizsync.backend.orchestrator


import android.util.Log
import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.backend.sync.SyncTurnoManager
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.AbsenceWindowCalculator
import java.time.LocalDate
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

    /**
     * Sincronizza i turni dalla cache locale a Firebase
     * @param weekStart Data di inizio settimana
     * @return Resource<String> con messaggio di successo o errore
     */
    suspend fun syncTurniToFirebase(weekStart: LocalDate): Resource<String> {
        return try {
            // Calcola i bounds della settimana
            val (startDate, endDate) = AbsenceWindowCalculator.getWeekBounds(weekStart)

            // Recupera i turni non sincronizzati della settimana
            val turni = turnoDao.getTurniSettimana(startDate, endDate, isSynced = false)

            if (turni.isEmpty()) {
                return Resource.Empty
            }

            var turniSincronizzati = 0
            var turniEliminati = 0

            // Processa ogni turno
            for (turno in turni) {
                when {
                    !turno.isSynced && !turno.isDeleted -> {
                        if (turno.idFirebase.isEmpty()) {
                            // Nuovo turno → aggiungi su Firebase
                            when (val result = turnoRepository.addTurnoToFirebase(turno.toDomain())) {
                                is Resource.Success -> {
                                    val firebaseId = result.data
                                    turnoDao.updateTurnoSyncStatus(turno.copy(idFirebase = firebaseId, isSynced = true))
                                    turniSincronizzati++
                                }
                                is Resource.Error -> {
                                    return Resource.Error("Errore aggiunta turno: ${result.message}")
                                }
                                is Resource.Empty -> {
                                    return Resource.Error("Errore: risposta vuota da Firebase")
                                }
                            }
                        } else {
                            // Turno modificato → aggiorna su Firebase
                            when (val result = turnoRepository.updateTurnoOnFirebase(turno.toDomain())) {
                                is Resource.Success -> {
                                    turnoDao.updateTurnoSyncStatus(
                                        turno.copy(isSynced = true)
                                    )
                                    turniSincronizzati++
                                }
                                is Resource.Error -> {
                                    return Resource.Error("Errore aggiornamento turno: ${result.message}")
                                }
                                is Resource.Empty -> {
                                    return Resource.Error("Errore: turno non trovato su Firebase")
                                }
                            }
                        }
                    }

                    turno.isDeleted -> {
                        // Turno eliminato → cancella da Firebase e rimuovi dalla cache
                        turno.idFirebase?.let { firebaseId ->
                            when (val result = turnoRepository.deleteTurnoFromFirebase(firebaseId)) {
                                is Resource.Success -> {
//                                    turnoRepository.deleteTurnoFromCache(turno)
                                    turniEliminati++
                                }
                                is Resource.Error -> {
                                    return Resource.Error("Errore eliminazione turno: ${result.message}")
                                }
                                is Resource.Empty -> {
                                    // Turno già eliminato da Firebase, rimuovi solo dalla cache
//                                    turnoRepository.deleteTurnoFromCache(turno)
                                    turniEliminati++
                                }
                            }
                        } ?: run {
                            // Turno senza firebaseId, rimuovi solo dalla cache
//                            turnoRepository.deleteTurnoFromCache(turno)
                            turniEliminati++
                        }
                    }
                }
            }

            // Messaggio di successo
            val messaggio = buildString {
                if (turniSincronizzati > 0) {
                    append("$turniSincronizzati turni sincronizzati")
                }
                if (turniEliminati > 0) {
                    if (turniSincronizzati > 0) append(", ")
                    append("$turniEliminati turni eliminati")
                }
                if (turniSincronizzati == 0 && turniEliminati == 0) {
                    append("Nessuna modifica da sincronizzare")
                }
            }

            Resource.Success(messaggio)

        } catch (e: Exception) {
            Resource.Error("Errore durante la sincronizzazione: ${e.message}")
        }
    }

    suspend fun createMockTurno()
    {
        turnoRepository.createMockTurni()
    }


    suspend fun fetchTurniSettimana(startWeek: LocalDate): Resource<List<Turno>> {
        return try {
            val (startDate, endDate) = AbsenceWindowCalculator.getWeekBounds(startWeek)
            val turniEntities = turnoDao.fetchTurniSettimana(startDate, endDate)

            if (turniEntities.isEmpty()) {
                Log.d("TURNI_DEBUG", "📭 Nessun turno trovato nella settimana $startDate - $endDate")
                Resource.Empty
            } else {
                val domainTurni = turniEntities.toDomainList()
                Log.d("TURNI_DEBUG", "✅ Trovati ${domainTurni.size} turni nella settimana $startDate - $endDate")
                Resource.Success(domainTurni)
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "🚨 Errore nel recupero turni settimana: ${e.message}", e)
            Resource.Error(e.message ?: "Errore nel recupero turni settimana")
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
