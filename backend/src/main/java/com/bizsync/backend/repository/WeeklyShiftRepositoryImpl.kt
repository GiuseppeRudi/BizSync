package com.bizsync.backend.repository

import com.bizsync.backend.dto.WeeklyShiftDto
import com.bizsync.backend.remote.WeeklyShiftFirestore
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import android.util.Log
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.repository.WeeklyShiftRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyShiftRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
)  : WeeklyShiftRepository {

    companion object {
        private const val TAG = "WeeklyShiftRepo"
        private const val DEBUG_TAG = "WEEKLY_SHIFT_DEBUG"
    }

    private val collection = firestore.collection(WeeklyShiftFirestore.COLLECTION)

    override suspend fun getWeeklyShiftCorrente(
        weekStart: LocalDate,
        idAzienda: String
        ): Resource<WeeklyShift?> {
        // Usa accesso diretto invece della query
        return getWeeklyShift(idAzienda, weekStart)
    }

    override suspend fun getThisWeekPublishedShift(
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

    override suspend fun getWeeklyShift(
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

    override suspend fun createWeeklyShift(weeklyShift: WeeklyShift): Resource<String> {
        return try {
            // Validazione dati essenziali
            if (weeklyShift.idAzienda.isBlank()) {
                return Resource.Error("ID Azienda mancante")
            }

            Log.d(TAG, "🔨 Creazione pianificazione: $weeklyShift")

            val documentId = weeklyShift.id
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



    override suspend fun updateWeeklyShiftStatus(
        weeklyShift: WeeklyShift,
        ): Resource<Unit> {
        return try {

            val documentId = weeklyShift.id

            val weeklyShiftDto = weeklyShift.toDto()

            // Validazione status
            if (!WeeklyShiftFirestore.Validation.VALID_STATUS.contains(weeklyShiftDto.status)) {
                return Resource.Error("Status non valido: ${weeklyShiftDto.status}")
            }

            collection
                .document(documentId)
                .update(WeeklyShiftFirestore.Fields.STATUS, weeklyShiftDto.status)
                .await()

            Log.d(DEBUG_TAG, "✅ WeeklyShift aggiornato correttamente su Firebase con ID: ${weeklyShift.id}")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ Errore durante l'aggiornamento WeeklyShift", e)
            Resource.Error("Errore durante l'aggiornamento: ${e.message}")
        }
    }


    private fun generateDocumentId(idAzienda: String, weekStart: LocalDate): String {
        val weekKey = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return WeeklyShiftFirestore.Paths.generateDocumentId(idAzienda, weekKey)
    }
}