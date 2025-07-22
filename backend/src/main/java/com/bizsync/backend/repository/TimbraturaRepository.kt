package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.backend.dto.TimbraturaDto
import com.bizsync.backend.mapper.TimbraturaMapper
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toEntity
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class TimbraturaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val timbraturaDao: TimbraturaDao
) {

    private val collection = firestore.collection("timbrature")
    private val TAG = "TimbraturaRepo"


    suspend fun syncTimbratureForUserInRange(
        userId: String,
        aziendaId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        try {
            Log.d("TimbraturaRepository", "Sincronizzazione timbrature per utente $userId dal $startDate al $endDate")

            // Converte le date in Timestamp per Firebase
            val startTimestamp = startDate.atStartOfDay()
            val endTimestamp = endDate.atTime(23, 59, 59)

            // Query Firebase per timbrature dell'utente nel range di date
            val timbratureFromFirebase = firestore.collection("timbrature")
                .whereEqualTo("idAzienda", aziendaId)
                .whereEqualTo("idDipendente", userId)
                .whereGreaterThanOrEqualTo("dataOraTimbratura", startTimestamp.toString())
                .whereLessThanOrEqualTo("dataOraTimbratura", endTimestamp.toString())
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
                        Log.e("TimbraturaRepository", "Errore conversione timbratura ${doc.id}: ${e.message}")
                        null
                    }
                }

            Log.d("TimbraturaRepository", "Trovate ${timbratureFromFirebase.size} timbrature su Firebase")

            // Salva tutte le timbrature in cache locale
            timbratureFromFirebase.forEach { timbratura ->
                try {
                    timbraturaDao.insert(timbratura.toEntity())
                    Log.d("TimbraturaRepository", "Timbratura ${timbratura.id} salvata in cache")
                } catch (e: Exception) {
                    Log.e("TimbraturaRepository", "Errore salvataggio timbratura ${timbratura.id}: ${e.message}")
                }
            }

            Log.d("TimbraturaRepository", "Sincronizzazione timbrature completata")

        } catch (e: Exception) {
            Log.e("TimbraturaRepository", "Errore nella sincronizzazione timbrature: ${e.message}")
            throw Exception("Errore nel sincronizzare le timbrature da Firebase: ${e.message}")
        }
    }

    suspend fun getTimbratureByAzienda(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Timbratura>> = try {
        // Calcola range
        val startTs = startDate.atStartOfDay().toFirebaseTimestamp()
        val endOfDay = LocalTime.of(23, 59, 59, 999_000_000)
        val endTs = endDate.atTime(endOfDay).toFirebaseTimestamp()

        Log.d(TAG, "startTs: $startTs, endTs: $endTs")
        Log.d(TAG, "Azienda: $idAzienda")


        // Query su Firebase
        val snap = collection
            .whereEqualTo("idAzienda", idAzienda)
            .whereGreaterThanOrEqualTo("timestamp", startTs)
            .whereLessThanOrEqualTo("timestamp", endTs)
            .get()
            .await()

        // Mappa DTO → Domain
        val timbrature = snap.documents.mapNotNull { doc ->
            doc.toObject(TimbraturaDto::class.java)
                ?.copy(idFirebase = doc.id)
                ?.let { TimbraturaMapper.toDomain(it) }
        }

        Log.d(TAG, " $timbrature ")


        // Sincronizza cache
        timbraturaDao.insertAll(timbrature.map { it.toEntity() })

        Resource.Success(timbrature)
    } catch (e: Exception) {
        Log.e(TAG, "Errore recupero timbrature", e)
        Resource.Error("Errore recupero timbrature: ${e.message}")
    }

    suspend fun addTimbratura(timbratura: Timbratura): Resource<String> = try {
        // Serializza Domain → DTO
        val dto = TimbraturaMapper.toDto(timbratura)
        // Inserisci su Firebase
        val ref = collection.add(dto).await()
        val idFb = ref.id

        // Aggiorna cache con l’ID Firebase
        timbraturaDao.insert(timbratura.copy(idFirebase = idFb).toEntity())

        Resource.Success(idFb)
    } catch (e: Exception) {
        Log.e(TAG, "Errore addTimbratura", e)
        Resource.Error("Errore aggiunta timbratura: ${e.message}")
    }

    suspend fun updateTimbratura(timbratura: Timbratura): Resource<Unit> = try {
        // Serializza e aggiorna cache
        timbraturaDao.update(timbratura.toEntity())
        // Aggiorna su Firebase
        if (timbratura.idFirebase.isNotBlank()) {
            val dto = TimbraturaMapper.toDto(timbratura)
            collection.document(timbratura.idFirebase).set(dto).await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Errore updateTimbratura", e)
        Resource.Error("Errore aggiornamento timbratura: ${e.message}")
    }

    suspend fun getTimbratureByTurno(idTurno: String): Resource<List<Timbratura>> = try {
        // Prima cache
        val cached = timbraturaDao.getByTurno(idTurno)
        if (cached.isNotEmpty()) {
            Resource.Success(cached.map { it.toDomain() })
        } else {
            // Poi Firebase
            val snap = collection
                .whereEqualTo("idTurno", idTurno)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(TimbraturaDto::class.java)
                    ?.copy(idFirebase = doc.id)
                    ?.let { TimbraturaMapper.toDomain(it) }
            }
            list.forEach { timbraturaDao.insert(it.toEntity()) }
            Resource.Success(list)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Errore getTimbratureByTurno", e)
        Resource.Error("Errore recupero timbrature turno: ${e.message}")
    }

    suspend fun getTimbratureAnomale(idAzienda: String): Resource<List<Timbratura>> = try {
        val anomalies = timbraturaDao.getTimbratureAnomale(idAzienda)
        Resource.Success(anomalies.map { it.toDomain() })
    } catch (e: Exception) {
        Log.e(TAG, "Errore getTimbratureAnomale", e)
        Resource.Error("Errore recupero timbrature anomale: ${e.message}")
    }

    suspend fun verificaTimbratura(idTimbratura: String): Resource<Unit> = try {
        val entity = timbraturaDao.getById(idTimbratura)
            ?: return Resource.Error("Timbratura non trovata")
        val updated = entity.copy(verificataDaManager = true)
        timbraturaDao.update(updated)
        if (updated.idFirebase.isNotBlank()) {
            collection.document(updated.idFirebase)
                .update("verificataDaManager", true)
                .await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Errore verificaTimbratura", e)
        Resource.Error("Errore verifica timbratura: ${e.message}")
    }
}
