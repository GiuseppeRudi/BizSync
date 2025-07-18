package com.bizsync.backend.repository

import kotlinx.coroutines.tasks.await
import android.util.Log
import com.bizsync.backend.dto.TurnoDto
import com.bizsync.backend.remote.TurniFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import com.bizsync.backend.mapper.toDomainList
import com.bizsync.backend.mapper.toDto
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
)  {

    companion object {
        private const val COLLECTION_NAME = "turni"
        private const val TAG = "TurnoRepository"
    }


    private val collection = firestore.collection(COLLECTION_NAME)

    /**
     * Aggiunge un nuovo turno a Firebase
     * @param turno Turno da aggiungere
     * @return Resource<String> con l'ID del documento Firebase
     */
    suspend fun addTurnoToFirebase(turno: Turno): Resource<String> {
        return try {
            val documentRef = firestore
                .collection("turni")
                .add(turno.toDto())
                .await()

            Resource.Success(documentRef.id)
        } catch (e: Exception) {
            Resource.Error("Errore aggiunta turno a Firebase: ${e.message}")
        }
    }

    /**
     * Aggiorna un turno esistente su Firebase
     * @param turno Turno da aggiornare
     * @return Resource<Unit> risultato dell'operazione
     */
    suspend fun updateTurnoOnFirebase(turno: Turno): Resource<Unit> {
        return try {
            if (turno.idFirebase.isEmpty()) {
                return Resource.Error("Turno non ha firebaseId")
            }

            firestore
                .collection("turni")
                .document(turno.idFirebase)
                .set(turno.toDto())
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore aggiornamento turno su Firebase: ${e.message}")
        }
    }

    /**
     * Elimina un turno da Firebase
     * @param firebaseId ID del documento Firebase
     * @return Resource<Unit> risultato dell'operazione
     */
    suspend fun deleteTurnoFromFirebase(firebaseId: String): Resource<Unit> {
        return try {
            firestore
                .collection("turni")
                .document(firebaseId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore eliminazione turno da Firebase: ${e.message}")
        }
    }


    suspend fun getTurniRangeByAzienda(
        idAzienda: String,
        startRange: LocalDate,
        endRange: LocalDate,
        idEmployee: String? = null
    ): Resource<List<Turno>> {
        return try {
            val startTimestamp = startRange.toFirebaseTimestamp()
            val endTimestamp = endRange.toFirebaseTimestamp()

            var query = firestore.collection(TurniFirestore.COLLECTION)
                .whereEqualTo("idAzienda", idAzienda)
                .whereGreaterThanOrEqualTo("data", startTimestamp)
                .whereLessThan("data", endTimestamp)

            // se idEmployee è specificato, controlla che sia contenuto nell'array idDipendenti
            if (idEmployee != null) {
                query = query.whereArrayContains("idDipendenti", idEmployee)
            }

            val result = query.get().await()

            val turni = result.mapNotNull { document ->
                document.toObject(TurnoDto::class.java)?.copy(idFirebase = document.id)
            }

            Log.d("TURNI_DEBUG", "🔍 Recuperati ${turni.size} turni per azienda $idAzienda")

            if (turni.isEmpty()) {
                Resource.Empty
            } else {
                Resource.Success(turni.toDomainList())
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "❌ Errore durante getTurniByAzienda: ${e.message}")
            Resource.Error("Errore durante il recupero dei turni per l'azienda")
        }
    }



}

