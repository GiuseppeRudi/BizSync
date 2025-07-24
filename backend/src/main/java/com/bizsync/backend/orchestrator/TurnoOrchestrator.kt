package com.bizsync.backend.orchestrator


import android.util.Log
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.backend.sync.SyncTurnoManager
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.WeeklyWindowCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class TurnoOrchestrator @Inject constructor(
    private val turnoRepository: TurnoRepository,
    private val turnoDao: TurnoDao,
    private val syncTurnoManager: SyncTurnoManager
) {

    suspend fun deleteOldCachedData(today: LocalDate = LocalDate.now()) {
        val cutoffDate = today.minusDays(90)
        val endOfWeek = cutoffDate.with(DayOfWeek.SUNDAY)
        turnoDao.deleteOlderThan(endOfWeek)
    }


    suspend fun syncTurniToFirebase(weekStart: LocalDate): Resource<String> {
        return try {
            // Calcola i bounds della settimana
            val (startDate, endDate) = WeeklyWindowCalculator.getWeekBounds(weekStart)

            // Recupera i turni non sincronizzati della settimana
            val turni = turnoDao.getTurniSettimana(startDate, endDate, isSynced = false)

            if (turni.isEmpty()) {
                return Resource.Empty
            }

            var turniSincronizzati = 0
            var turniEliminati = 0

            for (turno in turni) {
                when {
                    !turno.isSynced && !turno.isDeleted -> {
                        if (turno.idFirebase.isEmpty()) {
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
                        turno.idFirebase.let { firebaseId ->
                            when (val result = turnoRepository.deleteTurnoFromFirebase(firebaseId)) {
                                is Resource.Success -> {
                                    turnoDao.deleteTurno(turno)
                                    turniEliminati++
                                }

                                is Resource.Error -> {
                                    return Resource.Error("Errore eliminazione turno: ${result.message}")
                                }

                                is Resource.Empty -> {
                                    turnoDao.deleteTurno(turno)
                                    turniEliminati++
                                }
                            }
                        }
                    }
                }
            }

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


    suspend fun fetchTurniSettimana(
        startWeek: LocalDate,
        idAzienda: String? = null,
        idUser: String? = null
    ): Resource<List<Turno>> {
        return try {
            val currentWeekStart = WeeklyWindowCalculator.getCurrentWeekStart()

            val (windowStart, windowEnd) = if (idUser == null) {
                // Manager: usa finestra manager
                WeeklyWindowCalculator.calculateWindowForManager(currentWeekStart)
            } else {
                //  Employee: usa finestra employee
                WeeklyWindowCalculator.calculateWindowForEmployee(currentWeekStart)
            }

            if (startWeek in windowStart..windowEnd) {
                //  CASO 1: dentro la finestra
                val (startDate, endDate) = WeeklyWindowCalculator.getWeekBounds(startWeek)

                //  DAO: Manager vs Employee
                val turniEntities = turnoDao.fetchTurniSettimana(startDate, endDate)



                if (turniEntities.isEmpty()) {
                    Log.d("TURNI_DEBUG", " Nessun turno trovato nella settimana $startDate - $endDate ${if (idUser != null) "per utente $idUser" else ""}")
                    Resource.Empty
                } else {
                    val domainTurni = turniEntities.toDomainList()
                    Log.d("TURNI_DEBUG", " Trovati ${domainTurni.size} turni nella settimana $startDate - $endDate ${if (idUser != null) "per utente $idUser" else ""}")
                    Resource.Success(domainTurni)
                }
            } else {
                val (startDate, endDate) = WeeklyWindowCalculator.getWeekBounds(startWeek)

                // DAO: Manager vs Employee
                val turniEntities = turnoDao.fetchTurniSettimana(startDate, endDate)

                if (turniEntities.isNotEmpty()) {
                    //  Caso 1: Turni trovati in cache
                    val domainTurni = turniEntities.toDomainList()
                    Log.d("TURNI_DEBUG", " Trovati ${domainTurni.size} turni nella settimana $startDate - $endDate (da cache) ${if (idUser != null) "per utente $idUser" else ""}")
                    Resource.Success(domainTurni)
                } else if (idAzienda != null) {
                    //  Caso 2: Cache vuota → fetch da Firebase
                    Log.d("TURNI_DEBUG", " Cache vuota, recupero da Firebase settimana $startDate - $endDate ${if (idUser != null) "per utente $idUser" else ""}")

                    //  REPOSITORY: Manager vs Employee
                    val result = if (idUser == null) {
                        // Manager: prende tutti i turni dell'azienda
                        turnoRepository.getTurniRangeByAzienda(idAzienda, startDate, endDate)
                    } else {
                        // Employee: prende solo i turni dell'utente specifico
                        turnoRepository.getTurniRangeByAzienda(idAzienda, startDate, endDate, idUser)
                    }

                    when (result) {
                        is Resource.Success -> {
                            val turni = result.data
                            if (turni.isNotEmpty()) {
                                // Salva in cache
                                turnoDao.insertAll(turni.toEntityList())
                                Log.d("TURNI_DEBUG", " Turni salvati in cache")

                                // Ritorna dalla cache con il filtro appropriato
                                val updated = turnoDao.fetchTurniSettimana(startDate, endDate).toDomainList()

                                Resource.Success(updated)
                            } else {
                                Log.d("TURNI_DEBUG", " Nessun turno disponibile da Firebase")
                                Resource.Empty
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TURNI_DEBUG", " Errore da Firebase: ${result.message}")
                            result
                        }
                        else -> {
                            Log.d("TURNI_DEBUG", " Firebase ha restituito uno stato inatteso")
                            Resource.Empty
                        }
                    }
                } else {
                    Resource.Empty
                }
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "🚨 Errore nel recupero turni settimana: ${e.message}", e)
            Resource.Error(e.message ?: "Errore nel recupero turni settimana")
        }
    }

    suspend fun getTurni(idAzienda: String, idEmployee : String?, forceRefresh: Boolean = false): Resource<List<Turno>> {
        return try {
            if (forceRefresh) {
                Log.d("TURNI_DEBUG", "🔄 Force sync attivato per azienda $idAzienda")

                val syncResult = syncTurnoManager.forceSync(idAzienda, idEmployee)
                if (syncResult is Resource.Error) {
                    Log.e("TURNI_DEBUG", "❌ Errore durante forceSync: ${syncResult.message}")
                }

                val cachedEntities = turnoDao.getTurni()
                Log.d("TURNI_DEBUG", "📦 Recuperati ${cachedEntities.size} turni dalla cache dopo forceSync")
                Resource.Success(cachedEntities.toDomainList())
            } else {
                Log.d("TURNI_DEBUG", "⚙️ Sync intelligente per azienda $idAzienda")

                when (val result = syncTurnoManager.syncIfNeeded(idAzienda,idEmployee)) {
                    is Resource.Success -> {
                        val domainTurni = turnoDao.getTurni().toDomainList()
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
