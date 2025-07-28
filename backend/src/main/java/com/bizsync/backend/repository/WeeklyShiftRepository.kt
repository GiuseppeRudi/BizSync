package com.bizsync.backend.repository

import com.bizsync.backend.dto.WeeklyShiftDto
import com.bizsync.backend.remote.WeeklyShiftFirestore
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import android.util.Log
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyShiftRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "WeeklyShiftRepo"
        private const val DEBUG_TAG = "WEEKLY_SHIFT_DEBUG"
    }

    private val collection = firestore.collection(WeeklyShiftFirestore.COLLECTION)

    suspend fun getWeeklyShiftCorrente(weekStart: LocalDate): Resource<WeeklyShift?> {
        return try {
            val weekStartString = weekStart.toString()
            Log.d(TAG, "🔍 Ricerca weekly shift corrente per weekStart: $weekStartString")

            val snapshot = collection
                .whereEqualTo(WeeklyShiftFirestore.Fields.WEEK_START, weekStartString)
                .limit(WeeklyShiftFirestore.QueryLimits.MAX_RESULTS.toLong())
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val weeklyShift = document
                    .toObject(WeeklyShiftDto::class.java)
                    ?.toDomain(document.id)

                Log.d(TAG, "✅ Weekly shift trovato con status: ${weeklyShift?.status}")
                Resource.Success(weeklyShift)
            } else {
                Log.d(TAG, "❌ Nessuna weekly shift trovata per $weekStartString")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore nel recupero weekly shift", e)
            Resource.Error("Errore nel recupero weekly shift: ${e.message}")
        }
    }

    suspend fun getThisWeekPublishedShift(
        idAzienda: String,
        weekStart: LocalDate
    ): Resource<WeeklyShift?> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)
            Log.d(TAG, "🔍 Ricerca pianificazione pubblicata per questa settimana: $documentId")

            // Prendo il documento
            val snapshot = collection.document(documentId).get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "❌ Nessuna pianificazione trovata per $documentId")
                return Resource.Success(null)
            }

            // Deserializzo
            val dto = snapshot.toObject(WeeklyShiftDto::class.java)
            val domain = dto?.toDomain(snapshot.id)

            // Controllo lo status
            return if (domain != null && domain.status != WeeklyShiftStatus.NOT_PUBLISHED) {
                Log.d(TAG, "✅ Pianificazione valida trovata: status=${domain.status}")
                Resource.Success(domain)
            } else {
                Log.d(TAG, "⚠️ Pianificazione trovata ma NON pubblicata (status=${domain?.status})")
                Resource.Success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazione pubblicata", e)
            Resource.Error("Errore recupero pianificazione: ${e.message}")
        }
    }

    suspend fun getWeeklyShift(
        idAzienda: String,
        weekStart: LocalDate
    ): Resource<WeeklyShift?> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)

            Log.d(TAG, "🔍 Ricerca pianificazione: $documentId")

            val snapshot = collection.document(documentId).get().await()

            if (snapshot.exists()) {
                val weeklyShift = snapshot.toObject(WeeklyShiftDto::class.java)
                    ?.toDomain(snapshot.id)

                Log.d(TAG, "✅ Pianificazione trovata: ${weeklyShift?.status}")
                Resource.Success(weeklyShift)
            } else {
                Log.d(TAG, "❌ Nessuna pianificazione trovata per $documentId")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazione", e)
            Resource.Error("Errore recupero pianificazione: ${e.message}")
        }
    }

    suspend fun createWeeklyShift(weeklyShift: WeeklyShift): Resource<String> {
        return try {
            // Validazione dati essenziali
            if (weeklyShift.idAzienda.isBlank()) {
                return Resource.Error("ID Azienda mancante")
            }

            val documentId = generateDocumentId(weeklyShift.idAzienda, weeklyShift.weekStart)
            val firestoreData = weeklyShift.toDto()

            Log.d(TAG, "🔨 Creazione pianificazione: $documentId")
            Log.d(TAG, "📋 Status pianificazione: ${weeklyShift.status}")

            collection.document(documentId).set(firestoreData).await()

            Log.d(TAG, "✅ Pianificazione creata con successo")
            Resource.Success(documentId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore creazione pianificazione", e)
            Resource.Error("Errore creazione pianificazione: ${e.message}")
        }
    }

    suspend fun updateWeeklyShiftStatus(
        weeklyShiftId: String,
        newStatus: WeeklyShiftStatus
    ): Resource<Unit> {
        return try {
            Log.d(DEBUG_TAG, "🔄 Inizio aggiornamento WeeklyShift con ID: $weeklyShiftId")
            Log.d(DEBUG_TAG, "📊 Nuovo status: ${newStatus.name}")

            // Validazione status
            if (!WeeklyShiftFirestore.Validation.VALID_STATUS.contains(newStatus.name)) {
                return Resource.Error("Status non valido: ${newStatus.name}")
            }

            collection
                .document(weeklyShiftId)
                .update(WeeklyShiftFirestore.Fields.STATUS, newStatus.name)
                .await()

            Log.d(DEBUG_TAG, "✅ WeeklyShift aggiornato correttamente su Firebase con ID: $weeklyShiftId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ Errore durante l'aggiornamento WeeklyShift", e)
            Resource.Error("Errore durante l'aggiornamento: ${e.message}")
        }
    }

    // Overload per compatibilità con il codice esistente
    suspend fun updateWeeklyShiftStatus(weeklyShift: WeeklyShift): Resource<WeeklyShift> {
        return try {
            Log.d(DEBUG_TAG, "🔄 Inizio aggiornamento WeeklyShift con ID: ${weeklyShift.id}")
            Log.d(DEBUG_TAG, "📋 Dati da aggiornare: ${weeklyShift.toDto()}")

            val updateResult = updateWeeklyShiftStatus(weeklyShift.id, weeklyShift.status)

            when (updateResult) {
                is Resource.Success -> {
                    Log.d(DEBUG_TAG, "✅ WeeklyShift aggiornato correttamente")
                    Resource.Success(weeklyShift)
                }
                is Resource.Error -> {
                    Log.e(DEBUG_TAG, "❌ Errore aggiornamento WeeklyShift: ${updateResult.message}")
                    Resource.Error(updateResult.message)
                }
                else -> Resource.Error("Errore sconosciuto")
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ Errore durante l'aggiornamento WeeklyShift", e)
            Resource.Error(e.message ?: "Errore durante l'aggiornamento")
        }
    }

    suspend fun getWeeklyShiftsByStatus(
        idAzienda: String,
        status: WeeklyShiftStatus
    ): Resource<List<WeeklyShift>> {
        return try {
            Log.d(TAG, "🔍 Ricerca pianificazioni per azienda $idAzienda con status: ${status.name}")

            // Validazione status
            if (!WeeklyShiftFirestore.Validation.VALID_STATUS.contains(status.name)) {
                return Resource.Error("Status non valido: ${status.name}")
            }

            val snapshot = collection
                .whereEqualTo(WeeklyShiftFirestore.Fields.STATUS, status.name)
                .limit(WeeklyShiftFirestore.QueryLimits.MAX_RESULTS.toLong())
                .get()
                .await()

            val weeklyShifts = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(WeeklyShiftDto::class.java)?.toDomain(document.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Errore conversione weekly shift ${document.id}: ${e.message}")
                    null
                }
            }.filter { it.idAzienda == idAzienda } // Filtro per azienda a livello client

            Log.d(TAG, "✅ Trovate ${weeklyShifts.size} pianificazioni con status ${status.name}")
            Resource.Success(weeklyShifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazioni per status", e)
            Resource.Error("Errore recupero pianificazioni: ${e.message}")
        }
    }

    suspend fun deleteWeeklyShift(weeklyShiftId: String): Resource<Unit> {
        return try {
            Log.d(TAG, "🗑️ Eliminazione pianificazione: $weeklyShiftId")

            collection.document(weeklyShiftId).delete().await()

            Log.d(TAG, "✅ Pianificazione eliminata con successo")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore eliminazione pianificazione", e)
            Resource.Error("Errore eliminazione pianificazione: ${e.message}")
        }
    }

    private fun generateDocumentId(idAzienda: String, weekStart: LocalDate): String {
        val weekKey = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return WeeklyShiftFirestore.Paths.generateDocumentId(idAzienda, weekKey)
    }
}