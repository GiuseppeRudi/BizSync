package com.bizsync.sync.orchestrator


import android.util.Log
import com.bizsync.backend.repository.TurnoRemoteRepositoryImpl
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoSyncRepository
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.sync.sync.SyncTurnoManager
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlin.ranges.rangeTo

class TurnoOrchestrator @Inject constructor(
    private val turnoRemoteRepositoryImpl: TurnoRemoteRepositoryImpl,
    private val turnoDao: TurnoDao,
    private val syncTurnoManager: SyncTurnoManager
) : TurnoSyncRepository {

    override suspend fun deleteOldCachedData(currentDate: LocalDate) {
        val cutoffDate = currentDate.minusDays(90)
        val endOfWeek = cutoffDate.with(DayOfWeek.SUNDAY)
        turnoDao.deleteOlderThan(endOfWeek)
    }

    /**
     * Aggiorna immediatamente un turno sia in cache che su Firebase (se esiste)
     * Bypassa la logica di sincronizzazione normale
     * Usato per sostituzioni immediate (es. malattia)
     */
    override suspend fun updateTurnoImmediate(turno: Turno): Resource<String> {
        return try {
            Log.d("TURNO_UPDATE_IMMEDIATE", "🔄 Aggiornamento immediato turno: ${turno.id}")

            // 1. Recupera il turno esistente dalla cache
            val existingTurno = turnoDao.getTurnoById(turno.id)

            if (existingTurno == null) {
                Log.e("TURNO_UPDATE_IMMEDIATE", "❌ Turno non trovato in cache: ${turno.id}")
                return Resource.Error("Turno non trovato")
            }

            // 2. Prepara il turno aggiornato mantenendo i metadati
            val turnoEntity = turno.toEntity().copy(
                idFirebase = existingTurno.idFirebase,
                isSynced = true, // Marcalo come sincronizzato perché lo aggiorneremo subito
                isDeleted = false,
                createdAt = existingTurno.createdAt,
                updatedAt = com.google.firebase.Timestamp.now()
            )

            // 3. Aggiorna in cache locale
            turnoDao.update(turnoEntity)
            Log.d("TURNO_UPDATE_IMMEDIATE", "✅ Cache locale aggiornata")

            // 4. Se ha un ID Firebase, aggiorna anche su Firebase
            if (existingTurno.idFirebase.isNotEmpty()) {
                Log.d("TURNO_UPDATE_IMMEDIATE", "☁️ Aggiornamento su Firebase: ${existingTurno.idFirebase}")

                when (val result = turnoRemoteRepositoryImpl.updateTurnoOnFirebase(turno.copy(idFirebase = existingTurno.idFirebase))) {
                    is Resource.Success -> {
                        Log.d("TURNO_UPDATE_IMMEDIATE", "✅ Firebase aggiornato con successo")
                        Resource.Success("Turno aggiornato con successo (locale + remoto)")
                    }
                    is Resource.Error -> {
                        // Firebase fallito, ma cache locale è aggiornata
                        // Marco come non sincronizzato per retry futuro
                        turnoDao.updateTurnoSyncStatus(turno.id, existingTurno.idFirebase, false)
                        Log.w("TURNO_UPDATE_IMMEDIATE", "⚠️ Aggiornamento Firebase fallito: ${result.message}")
                        Resource.Success("Turno aggiornato localmente, sincronizzazione remota pendente")
                    }
                    else -> {
                        Resource.Success("Turno aggiornato localmente")
                    }
                }
            } else {
                Log.d("TURNO_UPDATE_IMMEDIATE", "✅ Turno aggiornato solo in locale (non presente su Firebase)")
                Resource.Success("Turno aggiornato con successo (solo locale)")
            }

        } catch (e: Exception) {
            Log.e("TURNO_UPDATE_IMMEDIATE", "🚨 Errore aggiornamento immediato: ${e.message}", e)
            Resource.Error("Errore durante l'aggiornamento: ${e.message}")
        }
    }

    /**
     * Elimina immediatamente un turno sia dalla cache che da Firebase (se esiste)
     * Bypassa la logica di soft delete e sincronizzazione
     * Usato per turni scoperti (es. malattia senza sostituto)
     */
    override suspend fun deleteTurnoImmediate(turnoId: String): Resource<String> {
        return try {
            Log.d("TURNO_DELETE_IMMEDIATE", "🗑️ Eliminazione immediata turno: $turnoId")

            // 1. Recupera il turno dalla cache
            val turnoEntity = turnoDao.getTurnoById(turnoId)

            if (turnoEntity == null) {
                Log.e("TURNO_DELETE_IMMEDIATE", "❌ Turno non trovato: $turnoId")
                return Resource.Error("Turno non trovato")
            }

            // 2. Se ha un ID Firebase, elimina da Firebase
            if (turnoEntity.idFirebase.isNotEmpty()) {
                Log.d("TURNO_DELETE_IMMEDIATE", "☁️ Eliminazione da Firebase: ${turnoEntity.idFirebase}")

                when (val result = turnoRemoteRepositoryImpl.deleteTurnoFromFirebase(turnoEntity.idFirebase)) {
                    is Resource.Success -> {
                        Log.d("TURNO_DELETE_IMMEDIATE", "✅ Eliminato da Firebase")
                    }
                    is Resource.Error -> {
                        Log.w("TURNO_DELETE_IMMEDIATE", "⚠️ Errore eliminazione Firebase: ${result.message}")
                        // Continua comunque con l'eliminazione locale
                    }
                    else -> {
                        Log.d("TURNO_DELETE_IMMEDIATE", "ℹ️ Turno non trovato su Firebase")
                    }
                }
            }

            // 3. Elimina dalla cache locale (hard delete)
            turnoDao.deleteTurno(turnoEntity)
            Log.d("TURNO_DELETE_IMMEDIATE", "✅ Eliminato dalla cache locale")

            Resource.Success("Turno eliminato completamente")

        } catch (e: Exception) {
            Log.e("TURNO_DELETE_IMMEDIATE", "🚨 Errore eliminazione immediata: ${e.message}", e)
            Resource.Error("Errore durante l'eliminazione: ${e.message}")
        }
    }

    // Metodo helper per verificare se un turno esiste su Firebase
    private suspend fun turnoExistsOnFirebase(firebaseId: String): Boolean {
        return try {
            if (firebaseId.isEmpty()) return false

            when (val result = turnoRemoteRepositoryImpl.getTurnoById(firebaseId)) {
                is Resource.Success -> true
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun saveTurno(
        turno: Turno,
        dipartimento: String,
        giornoSelezionato: LocalDate,
        idAzienda: String
    ): Resource<String> {
        return try {
            Log.d("TURNO_SAVE", "💾 Inizio salvataggio turno: ${turno.id}")

            // Controlla se il turno esiste già nella cache
            val existingTurno = turnoDao.getTurnoById(turno.id)
            val isNewTurno = existingTurno == null

            Log.d("TURNO_SAVE", "📋 Turno ${if (isNewTurno) "NUOVO" else "ESISTENTE"}: ${turno.id}")

            val turnoToSave = if (isNewTurno) {
                // Nuovo turno: aggiungi dati mancanti
                turno.copy(
                    dipartimento = dipartimento,
                    data = giornoSelezionato,
                    idAzienda = idAzienda
                )
            } else {
                // Turno esistente: mantieni i dati originali se non specificati
                turno.copy(
                    dipartimento = if (turno.dipartimento.isBlank()) dipartimento else turno.dipartimento,
                    data = if (turno.data == LocalDate.MIN) giornoSelezionato else turno.data,
                    idAzienda = if (turno.idAzienda.isBlank()) idAzienda else turno.idAzienda
                )
            }

            // Converti a Entity con flag di sincronizzazione
            val turnoEntity = turnoToSave.toEntity().copy(
                isSynced = false, // Sempre false per indicare che serve sincronizzazione
                isDeleted = false,
                createdAt = existingTurno?.createdAt ?: com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )

            if (isNewTurno) {
                // Inserimento nuovo turno
                turnoDao.insert(turnoEntity)
                Log.d("TURNO_SAVE", "✅ Nuovo turno inserito in cache: ${turno.titolo}")
                Resource.Success("Turno creato con successo")
            } else {
                // Aggiornamento turno esistente
                turnoDao.update(turnoEntity)
                Log.d("TURNO_SAVE", "✅ Turno aggiornato in cache: ${turno.titolo}")
                Resource.Success("Turno aggiornato con successo")
            }

        } catch (e: Exception) {
            Log.e("TURNO_SAVE", "🚨 Errore durante salvataggio turno: ${e.message}", e)
            Resource.Error("Errore durante il salvataggio: ${e.message}")
        }
    }

    override suspend fun deleteTurno(turnoId: String): Resource<String> {
        return try {
            Log.d("TURNO_DELETE", "🗑️ Tentativo eliminazione turno: $turnoId")

            // Recupera il turno dal database
            val turnoEntity = turnoDao.getTurnoById(turnoId)

            when {
                turnoEntity == null -> {
                    Log.e("TURNO_DELETE", "❌ Turno non trovato: $turnoId")
                    Resource.Error("Turno non trovato")
                }

                turnoEntity.isDeleted -> {
                    Log.w("TURNO_DELETE", "⚠️ Turno già eliminato: $turnoId")
                    Resource.Error("Il turno è già stato eliminato")
                }

                else -> {
                    // Effettua soft delete impostando isDeleted = true
                    val turnoEliminato = turnoEntity.copy(
                        isDeleted = true,
                        isSynced = false, // Marca come non sincronizzato per propagare la modifica
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    turnoDao.update(turnoEliminato)

                    Log.d("TURNO_DELETE", "✅ Turno eliminato (soft delete): ${turnoEntity.titolo}")
                    Resource.Success("Turno eliminato con successo")
                }
            }

        } catch (e: Exception) {
            Log.e("TURNO_DELETE", "🚨 Errore durante eliminazione turno: ${e.message}", e)
            Resource.Error("Errore durante l'eliminazione: ${e.message}")
        }
    }

    override suspend fun syncTurniToFirebase(weekStart: LocalDate): Resource<String> {
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
                            when (val result = turnoRemoteRepositoryImpl.addTurnoToFirebase(turno.toDomain())) {
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
                            when (val result = turnoRemoteRepositoryImpl.updateTurnoOnFirebase(turno.toDomain())) {
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
                            when (val result = turnoRemoteRepositoryImpl.deleteTurnoFromFirebase(firebaseId)) {
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


    override suspend fun fetchTurniSettimana(
        startWeek: LocalDate,
        idAzienda: String?,
        idUser: String?
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
                        turnoRemoteRepositoryImpl.getTurniRangeByAzienda(idAzienda, startDate, endDate,null)
                    } else {
                        // Employee: prende solo i turni dell'utente specifico
                        turnoRemoteRepositoryImpl.getTurniRangeByAzienda(idAzienda, startDate, endDate, idUser)
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

    override suspend fun getTurni(idAzienda: String, idEmployee : String?, forceRefresh: Boolean): Resource<List<Turno>> {
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


    suspend fun getTurniRangeByAzienda(
        idAzienda: String,
        startRange: LocalDate,
        endRange: LocalDate,
        idEmployee: String?
    ): Resource<List<Turno>> {
        return try {

            // 1. Prova prima dalla cache
            val cachedTurni = turnoDao.getTurniInRange(startRange, endRange)

            if (cachedTurni.isNotEmpty()) {
                return Resource.Success(cachedTurni.toDomainList())
            }

            // 2. Cache vuota - fetch da Firebase
            when (val result = turnoRemoteRepositoryImpl.getTurniRangeByAzienda(idAzienda, startRange, endRange, idEmployee)) {
                is Resource.Success -> {
                    val turni = result.data
                    if (turni.isNotEmpty()) {
                        // Salva in cache
                        turnoDao.insertAll(turni.toEntityList())
                    }
                    Resource.Success(turni)
                }
                is Resource.Error -> {
                    result
                }
                else -> {
                    Resource.Empty
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore recupero turni")
        }
    }

}
