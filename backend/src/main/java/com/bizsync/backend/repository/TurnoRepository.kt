package com.bizsync.backend.repository

import kotlinx.coroutines.tasks.await
import android.util.Log
import com.bizsync.backend.dto.TurnoDto
import com.bizsync.backend.remote.TurniFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import com.bizsync.backend.mapper.toDomainList
import com.bizsync.backend.mapper.toDto
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toEntity
import com.google.firebase.firestore.toObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val turnoDao : TurnoDao
)  {



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


    suspend fun syncTurniInRange(startDate: LocalDate, endDate: LocalDate) {
        try {
            val turniFromFirebase = firestore.collection("turni")
                .whereGreaterThanOrEqualTo("data", startDate.toString())
                .whereLessThanOrEqualTo("data", endDate.toString())
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<Turno>()?.copy(id = doc.id)
                }

            turniFromFirebase.forEach { turno ->
                turnoDao.insertTurno(turno.toEntity())
            }

        } catch (e: Exception) {
            throw Exception("Errore nel sync turni: ${e.message}")
        }
    }

    suspend fun syncAllTurni() {
        try {
            val allTurni = firestore.collection("turni")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<Turno>()?.copy(id = doc.id)
                }

            allTurni.forEach { turno ->
                turnoDao.insertTurno(turno.toEntity())
            }

        } catch (e: Exception) {
            throw Exception("Errore nel sync completo turni: ${e.message}")
        }
    }

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

    suspend fun syncTurniForUserInRange(
        userId: String,
        aziendaId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        try {
            Log.d("TurnoRepository", "Sincronizzazione turni per utente $userId dal $startDate al $endDate")

            val turniFromFirebase = firestore.collection("turni")
                .whereEqualTo("idAzienda", aziendaId)
                .whereArrayContains("idDipendenti", userId)
                .whereGreaterThanOrEqualTo("data", startDate.toString())
                .whereLessThanOrEqualTo("data", endDate.toString())
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject<Turno>()?.copy(
                            id = doc.id,
                            idFirebase = doc.id
                        )
                    } catch (e: Exception) {
                        Log.e("TurnoRepository", "Errore conversione turno ${doc.id}: ${e.message}")
                        null
                    }
                }

            Log.d("TurnoRepository", "Trovati ${turniFromFirebase.size} turni su Firebase")

            // Salva tutti i turni in cache locale
            turniFromFirebase.forEach { turno ->
                try {
                    turnoDao.insertTurno(turno.toEntity())
                    Log.d("TurnoRepository", "Turno ${turno.id} salvato in cache")
                } catch (e: Exception) {
                    Log.e("TurnoRepository", "Errore salvataggio turno ${turno.id}: ${e.message}")
                }
            }

            Log.d("TurnoRepository", "Sincronizzazione turni completata")

        } catch (e: Exception) {
            Log.e("TurnoRepository", "Errore nella sincronizzazione turni: ${e.message}")
            throw Exception("Errore nel sincronizzare i turni da Firebase: ${e.message}")
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

            if (idEmployee != null) {
                query = query.whereArrayContains("idDipendenti", idEmployee)
            }

            val result = query.get().await()

            val turni = result.mapNotNull { document ->
                document.toObject(TurnoDto::class.java).copy(idFirebase = document.id)
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

