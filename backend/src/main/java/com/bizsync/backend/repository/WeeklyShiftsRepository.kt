package com.bizsync.backend.repository

import com.bizsync.backend.dto.WeeklyShiftDto
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
)  {

    companion object {
        private const val COLLECTION_NAME = "weekly_shifts"
        private const val TAG = "WeeklyShiftRepo"
    }

    private val collection = firestore.collection(COLLECTION_NAME)


    suspend fun getWeeklyShiftCorrente(weekStart: LocalDate): Resource<WeeklyShift?> {
        return try {
            val weekStartString = weekStart.toString()
            Log.d(TAG, "🔍 Ricerca weekly shift corrente per weekStart: $weekStartString")

            val snapshot = collection
                .whereEqualTo("weekStart", weekStartString)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val weeklyShift = document
                    .toObject(WeeklyShiftDto::class.java)
                    ?.toDomain(document.id)

                Log.d(TAG, " Weekly shift trovato con status: ${weeklyShift?.status}")
                Resource.Success(weeklyShift)
            } else {
                Log.d(TAG, " Nessuna weekly shift trovata per $weekStartString")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, " Errore nel recupero weekly shift: ${e.message}")
            Resource.Error("Errore nel recupero weekly shift: ${e.message}")
        }
    }

    suspend fun getThisWeekPublishedShift(
        idAzienda: String,
        weekStart : LocalDate
    ): Resource<WeeklyShift?> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)
            Log.d(TAG, " Ricerca pianificazione pubblicata per questa settimana: $documentId")

            // Prendo il documento
            val snapshot = collection.document(documentId).get().await()
            if (!snapshot.exists()) {
                Log.d(TAG, "Nessuna pianificazione trovata per $documentId")
                return Resource.Success(null)
            }

            // Deserializzo
            val dto = snapshot.toObject(WeeklyShiftDto::class.java)
            val domain = dto?.toDomain(snapshot.id)

            // Controllo lo status
            return if (domain != null && domain.status != WeeklyShiftStatus.NOT_PUBLISHED) {
                Log.d(TAG, " Pianificazione valida trovata: status=${domain.status}")
                Resource.Success(domain)
            } else {
                Log.d(TAG, " Pianificazione trovata ma NON pubblicata (status=${domain?.status})")
                Resource.Success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, " Errore recupero pianificazione pubblicata: ${e.message}")
            Resource.Error("Errore recupero pianificazione: ${e.message}")
        }
    }


    suspend fun getWeeklyShift(
        idAzienda: String,
        weekStart: LocalDate
    ): Resource<WeeklyShift?> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)

            Log.d(TAG, " Ricerca pianificazione: $documentId")

            val snapshot = collection.document(documentId).get().await()

            if (snapshot.exists()) {
                val weeklyShift = snapshot.toObject(WeeklyShiftDto::class.java)
                    ?.toDomain(snapshot.id)

                Log.d(TAG, " Pianificazione trovata: ${weeklyShift?.status}")
                Resource.Success(weeklyShift)
            } else {
                Log.d(TAG, " Nessuna pianificazione trovata per $documentId")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, " Errore recupero pianificazione: ${e.message}")
            Resource.Error("Errore recupero pianificazione: ${e.message}")
        }
    }

     suspend fun createWeeklyShift(weeklyShift: WeeklyShift): Resource<String> {
        return try {
            val documentId = generateDocumentId(weeklyShift.idAzienda, weeklyShift.weekStart)
            val firestoreData = weeklyShift.toDto()

            Log.d(TAG, " Creazione pianificazione: $documentId")

            collection.document(documentId).set(firestoreData).await()

            Log.d(TAG, " Pianificazione creata con successo")
            Resource.Success(documentId)
        } catch (e: Exception) {
            Log.e(TAG, " Errore creazione pianificazione: ${e.message}")
            Resource.Error("Errore creazione pianificazione: ${e.message}")
        }
    }

    suspend fun updateWeeklyShiftStatus(weeklyShift: WeeklyShift): Resource<WeeklyShift> {
        return try {
            Log.d("WEEKLY_SHIFT_DEBUG", " Inizio aggiornamento WeeklyShift con ID: ${weeklyShift.id}")
            Log.d("WEEKLY_SHIFT_DEBUG", " Dati da aggiornare: ${weeklyShift.toDto()}")

            firestore.collection("weekly_shifts")
                .document(weeklyShift.id)
                .update("status", weeklyShift.status.name)
                .await()


            Log.d("WEEKLY_SHIFT_DEBUG", " WeeklyShift aggiornato correttamente su Firebase con ID: ${weeklyShift.id}")
            Resource.Success(weeklyShift)
        } catch (e: Exception) {
            Log.e("WEEKLY_SHIFT_DEBUG", " Errore durante l'aggiornamento WeeklyShift: ${e.message}", e)
            Resource.Error(e.message ?: "Errore durante l'aggiornamento")
        }
    }


    private fun generateDocumentId(idAzienda: String, weekStart: LocalDate): String {
        val weekKey = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "${idAzienda}_$weekKey"
    }
}
