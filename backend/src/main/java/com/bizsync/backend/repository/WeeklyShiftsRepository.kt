package com.bizsync.backend.repository

import com.bizsync.backend.dto.WeeklyShiftDto
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus


import android.util.Log
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
            val weekStartString = weekStart.toString() // es. "2025-07-21"
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

                Log.d(TAG, "✅ Weekly shift trovato con status: ${weeklyShift?.status}")
                Resource.Success(weeklyShift)
            } else {
                Log.d(TAG, "📭 Nessuna weekly shift trovata per $weekStartString")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore nel recupero weekly shift: ${e.message}")
            Resource.Error("Errore nel recupero weekly shift: ${e.message}")
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
                Log.d(TAG, "📭 Nessuna pianificazione trovata per $documentId")
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazione: ${e.message}")
            Resource.Error("Errore recupero pianificazione: ${e.message}")
        }
    }

     suspend fun createWeeklyShift(weeklyShift: WeeklyShift): Resource<String> {
        return try {
            val documentId = generateDocumentId(weeklyShift.idAzienda, weeklyShift.weekStart)
            val firestoreData = weeklyShift.toDto()

            Log.d(TAG, "💾 Creazione pianificazione: $documentId")

            collection.document(documentId).set(firestoreData).await()

            Log.d(TAG, "✅ Pianificazione creata con successo")
            Resource.Success(documentId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore creazione pianificazione: ${e.message}")
            Resource.Error("Errore creazione pianificazione: ${e.message}")
        }
    }

    suspend fun updateWeeklyShiftStatus(weeklyShift: WeeklyShift): Resource<WeeklyShift> {
        return try {
            Log.d("WEEKLY_SHIFT_DEBUG", "🔄 Inizio aggiornamento WeeklyShift con ID: ${weeklyShift.id}")
            Log.d("WEEKLY_SHIFT_DEBUG", "📦 Dati da aggiornare: ${weeklyShift.toDto()}")

            firestore.collection("weekly_shifts")
                .document(weeklyShift.id)
                .update("status", weeklyShift.status.name)
                .await()


            Log.d("WEEKLY_SHIFT_DEBUG", "✅ WeeklyShift aggiornato correttamente su Firebase con ID: ${weeklyShift.id}")
            Resource.Success(weeklyShift)
        } catch (e: Exception) {
            Log.e("WEEKLY_SHIFT_DEBUG", "❌ Errore durante l'aggiornamento WeeklyShift: ${e.message}", e)
            Resource.Error(e.message ?: "Errore durante l'aggiornamento")
        }
    }


    suspend fun deleteWeeklyShift(
        idAzienda: String,
        weekStart: LocalDate
    ): Resource<Unit> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)

            Log.d(TAG, "🗑️ Eliminazione pianificazione: $documentId")

            collection.document(documentId).delete().await()

            Log.d(TAG, "✅ Pianificazione eliminata con successo")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore eliminazione pianificazione: ${e.message}")
            Resource.Error("Errore eliminazione pianificazione: ${e.message}")
        }
    }

     suspend fun getWeeklyShiftsByAzienda(
        idAzienda: String,
        limit: Int
    ): Resource<List<WeeklyShift>> {
        return try {
            Log.d(TAG, "📋 Recupero pianificazioni azienda: $idAzienda (limit: $limit)")

            val query = collection
                .whereEqualTo("idAzienda", idAzienda)
                .orderBy("weekStart", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            val snapshot = query.get().await()

            val weeklyShifts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WeeklyShiftDto::class.java)?.toDomain(doc.id)
            }

            Log.d(TAG, "✅ Trovate ${weeklyShifts.size} pianificazioni")
            Resource.Success(weeklyShifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazioni azienda: ${e.message}")
            Resource.Error("Errore recupero pianificazioni: ${e.message}")
        }
    }

     suspend fun getWeeklyShiftsByStatus(
        idAzienda: String,
        status: WeeklyShiftStatus
    ): Resource<List<WeeklyShift>> {
        return try {
            Log.d(TAG, "🔍 Recupero pianificazioni per stato: $status")

            val query = collection
                .whereEqualTo("idAzienda", idAzienda)
                .whereEqualTo("status", status.name)
                .orderBy("weekStart", Query.Direction.DESCENDING)

            val snapshot = query.get().await()

            val weeklyShifts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WeeklyShiftDto::class.java)?.toDomain(doc.id)
            }

            Log.d(TAG, "✅ Trovate ${weeklyShifts.size} pianificazioni con stato $status")
            Resource.Success(weeklyShifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazioni per stato: ${e.message}")
            Resource.Error("Errore recupero pianificazioni per stato: ${e.message}")
        }
    }

     suspend fun updateWeeklyShift(weeklyShift: WeeklyShift): Resource<Unit> {
        return try {
            val documentId = generateDocumentId(weeklyShift.idAzienda, weeklyShift.weekStart)
            val firestoreData = weeklyShift.toDto().copy(
                updatedAt = com.google.firebase.Timestamp.now()
            )

            Log.d(TAG, "🔄 Aggiornamento pianificazione completa: $documentId")

            collection.document(documentId).set(firestoreData).await()

            Log.d(TAG, "✅ Pianificazione aggiornata con successo")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore aggiornamento pianificazione: ${e.message}")
            Resource.Error("Errore aggiornamento pianificazione: ${e.message}")
        }
    }

     suspend fun getWeeklyShiftsInRange(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<WeeklyShift>> {
        return try {
            val startKey = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endKey = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            Log.d(TAG, "📅 Recupero pianificazioni nel range: $startKey → $endKey")

            val query = collection
                .whereEqualTo("idAzienda", idAzienda)
                .whereGreaterThanOrEqualTo("weekStart", startKey)
                .whereLessThanOrEqualTo("weekStart", endKey)
                .orderBy("weekStart", Query.Direction.ASCENDING)

            val snapshot = query.get().await()

            val weeklyShifts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(WeeklyShiftDto::class.java)?.toDomain(doc.id)
            }

            Log.d(TAG, "✅ Trovate ${weeklyShifts.size} pianificazioni nel range")
            Resource.Success(weeklyShifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero pianificazioni nel range: ${e.message}")
            Resource.Error("Errore recupero pianificazioni nel range: ${e.message}")
        }
    }

     suspend fun checkWeeklyShiftExists(
        idAzienda: String,
        weekStart: LocalDate
    ): Resource<Boolean> {
        return try {
            val documentId = generateDocumentId(idAzienda, weekStart)

            val snapshot = collection.document(documentId).get().await()
            val exists = snapshot.exists()

            Log.d(TAG, "🔍 Controllo esistenza $documentId: $exists")
            Resource.Success(exists)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore controllo esistenza: ${e.message}")
            Resource.Error("Errore controllo esistenza: ${e.message}")
        }
    }

     suspend fun getLatestWeeklyShift(idAzienda: String): Resource<WeeklyShift?> {
        return try {
            Log.d(TAG, "🕐 Recupero ultima pianificazione per azienda: $idAzienda")

            val query = collection
                .whereEqualTo("idAzienda", idAzienda)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)

            val snapshot = query.get().await()

            val latestShift = snapshot.documents.firstOrNull()
                ?.toObject(WeeklyShiftDto::class.java)
                ?.toDomain(snapshot.documents.first().id)

            Log.d(TAG, "✅ Ultima pianificazione: ${latestShift?.weekStart}")
            Resource.Success(latestShift)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore recupero ultima pianificazione: ${e.message}")
            Resource.Error("Errore recupero ultima pianificazione: ${e.message}")
        }
    }

    // UTILITY FUNCTIONS
    private fun generateDocumentId(idAzienda: String, weekStart: LocalDate): String {
        val weekKey = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "${idAzienda}_$weekKey"
    }
}
