package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.backend.dto.TimbraturaDto
import com.bizsync.backend.mapper.TimbraturaMapper
import com.bizsync.backend.remote.TimbraturaFirestore
import com.bizsync.cache.mapper.toEntity
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaRemoteRepository
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class TimbraturaRemoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val timbraturaDao: TimbraturaDao
) : TimbraturaRemoteRepository {

    private val collection = firestore.collection(TimbraturaFirestore.COLLECTION)

    companion object {
        private const val TAG = "TimbraturaRemoteRepositoryImpl"
    }

    override suspend fun syncTimbratureForUserInRange(
        userId: String,
        aziendaId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        try {
            Log.d(TAG, "Sincronizzazione timbrature per utente $userId dal $startDate al $endDate")

            val startTimestamp = startDate.atStartOfDay()
            val endTimestamp = endDate.atTime(23, 59, 59)

            val timbratureFromFirebase = collection
                .whereEqualTo(TimbraturaFirestore.Fields.ID_AZIENDA, aziendaId)
                .whereEqualTo(TimbraturaFirestore.Fields.ID_DIPENDENTE, userId)
                .whereGreaterThanOrEqualTo(
                    TimbraturaFirestore.Fields.DATA_ORA_TIMBRATURA,
                    startTimestamp.toString()
                )
                .whereLessThanOrEqualTo(
                    TimbraturaFirestore.Fields.DATA_ORA_TIMBRATURA,
                    endTimestamp.toString()
                )
                .limit(TimbraturaFirestore.QueryLimits.MAX_RESULTS.toLong())
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject<Timbratura>()?.copy(
                            id = doc.id,
                            idFirebase = doc.id
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Errore conversione timbratura ${doc.id}: ${e.message}")
                        null
                    }
                }

            Log.d(TAG, "Trovate ${timbratureFromFirebase.size} timbrature su Firebase")

            timbratureFromFirebase.forEach { timbratura ->
                try {
                    timbraturaDao.insert(timbratura.toEntity())
                    Log.d(TAG, "Timbratura ${timbratura.id} salvata in cache")
                } catch (e: Exception) {
                    Log.e(TAG, "Errore salvataggio timbratura ${timbratura.id}: ${e.message}")
                }
            }

            Log.d(TAG, "Sincronizzazione timbrature completata")

        } catch (e: Exception) {
            Log.e(TAG, "Errore nella sincronizzazione timbrature: ${e.message}")
            throw Exception("Errore nel sincronizzare le timbrature da Firebase: ${e.message}")
        }
    }

    override suspend fun getTimbratureByAzienda(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Timbratura>> = try {
        val startTs = startDate.atStartOfDay().toFirebaseTimestamp()
        val endOfDay = LocalTime.of(23, 59, 59, 999_000_000)
        val endTs = endDate.atTime(endOfDay).toFirebaseTimestamp()

        Log.d(TAG, "Query range: startTs=$startTs, endTs=$endTs")
        Log.d(TAG, "Azienda: $idAzienda")

        // Query su Firebase con costanti
        val snap = collection
            .whereEqualTo(TimbraturaFirestore.Fields.ID_AZIENDA, idAzienda)
            .whereGreaterThanOrEqualTo(TimbraturaFirestore.Fields.TIMESTAMP, startTs)
            .whereLessThanOrEqualTo(TimbraturaFirestore.Fields.TIMESTAMP, endTs)
            .limit(TimbraturaFirestore.QueryLimits.MAX_RESULTS.toLong())
            .get()
            .await()

        // Mappa DTO → Domain
        val timbrature = snap.documents.mapNotNull { doc ->
            try {
                doc.toObject(TimbraturaDto::class.java)
                    ?.copy(idFirebase = doc.id)
                    ?.let { TimbraturaMapper.toDomain(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Errore conversione timbratura ${doc.id}: ${e.message}")
                null
            }
        }

        Log.d(TAG, "Recuperate ${timbrature.size} timbrature da Firebase")

        // Salva in cache
        timbraturaDao.insertAll(timbrature.map { it.toEntity() })

        Resource.Success(timbrature)
    } catch (e: Exception) {
        Log.e(TAG, "Errore recupero timbrature", e)
        Resource.Error("Errore recupero timbrature: ${e.message}")
    }

    suspend fun addTimbratura(timbratura: Timbratura): Resource<String> {
        return try {
            // Validazione dati essenziali
            if (timbratura.idAzienda.isBlank() || timbratura.idDipendente.isBlank()) {
                return Resource.Error("Dati timbratura incompleti")
            }

            // Serializza Domain → DTO
            val dto = TimbraturaMapper.toDto(timbratura)

            // Inserisci su Firebase
            val ref = collection.add(dto).await()
            val idFb = ref.id

            Log.d(TAG, "Timbratura aggiunta su Firebase con ID: $idFb")

            // Salva in cache con ID Firebase
            timbraturaDao.insert(timbratura.copy(idFirebase = idFb).toEntity())

            Log.d(TAG, "Timbratura salvata in cache locale")

            Resource.Success(idFb)
        } catch (e: Exception) {
            Log.e(TAG, "Errore addTimbratura", e)
            Resource.Error("Errore aggiunta timbratura: ${e.message}")
        }
    }

    override suspend fun verificaTimbratura(idTimbratura: String): Resource<Unit> = try {
        Log.d(TAG, "Verifica timbratura: $idTimbratura")

        val entity = timbraturaDao.getById(idTimbratura)
            ?: return Resource.Error("Timbratura non trovata in cache locale")

        // Aggiorna in cache locale
        val updated = entity.copy(verificataDaManager = true)
        timbraturaDao.update(updated)

        Log.d(TAG, "Timbratura verificata in cache locale")

        // Aggiorna su Firebase se presente l'ID
        if (updated.idFirebase.isNotBlank()) {
            collection.document(updated.idFirebase)
                .update(TimbraturaFirestore.Fields.VERIFICATA_DA_MANAGER, true)
                .await()

            Log.d(TAG, "Timbratura verificata su Firebase: ${updated.idFirebase}")
        } else {
            Log.w(TAG, "ID Firebase mancante per timbratura $idTimbratura")
        }

        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Errore verificaTimbratura", e)
        Resource.Error("Errore verifica timbratura: ${e.message}")
    }

    // Metodi aggiuntivi per sfruttare le costanti

    suspend fun getTimbratureByDipartimento(
        idAzienda: String,
        dipartimento: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Timbratura>> = try {
        val startTs = startDate.atStartOfDay().toFirebaseTimestamp()
        val endOfDay = LocalTime.of(23, 59, 59, 999_000_000)
        val endTs = endDate.atTime(endOfDay).toFirebaseTimestamp()

        Log.d(TAG, "Query timbrature per dipartimento: $dipartimento")

        val snap = collection
            .whereEqualTo(TimbraturaFirestore.Fields.ID_AZIENDA, idAzienda)
            .whereEqualTo(TimbraturaFirestore.Fields.DIPARTIMENTO, dipartimento)
            .whereGreaterThanOrEqualTo(TimbraturaFirestore.Fields.TIMESTAMP, startTs)
            .whereLessThanOrEqualTo(TimbraturaFirestore.Fields.TIMESTAMP, endTs)
            .limit(TimbraturaFirestore.QueryLimits.MAX_RESULTS.toLong())
            .get()
            .await()

        val timbrature = snap.documents.mapNotNull { doc ->
            try {
                doc.toObject(TimbraturaDto::class.java)
                    ?.copy(idFirebase = doc.id)
                    ?.let { TimbraturaMapper.toDomain(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Errore conversione timbratura ${doc.id}: ${e.message}")
                null
            }
        }

        Resource.Success(timbrature)
    } catch (e: Exception) {
        Log.e(TAG, "Errore recupero timbrature per dipartimento", e)
        Resource.Error("Errore recupero timbrature per dipartimento: ${e.message}")
    }

    suspend fun getTimbratureNonVerificate(idAzienda: String): Resource<List<Timbratura>> = try {
        Log.d(TAG, "Query timbrature non verificate per azienda: $idAzienda")

        val snap = collection
            .whereEqualTo(TimbraturaFirestore.Fields.ID_AZIENDA, idAzienda)
            .whereEqualTo(TimbraturaFirestore.Fields.VERIFICATA_DA_MANAGER, false)
            .limit(TimbraturaFirestore.QueryLimits.MAX_RESULTS.toLong())
            .get()
            .await()

        val timbrature = snap.documents.mapNotNull { doc ->
            try {
                doc.toObject(TimbraturaDto::class.java)
                    ?.copy(idFirebase = doc.id)
                    ?.let { TimbraturaMapper.toDomain(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Errore conversione timbratura ${doc.id}: ${e.message}")
                null
            }
        }

        Log.d(TAG, "Trovate ${timbrature.size} timbrature non verificate")

        Resource.Success(timbrature)
    } catch (e: Exception) {
        Log.e(TAG, "Errore recupero timbrature non verificate", e)
        Resource.Error("Errore recupero timbrature non verificate: ${e.message}")
    }
}