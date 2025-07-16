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
    suspend fun createMockTurni(): Boolean {
//        return try {
//            val now = Timestamp.now()
//            val collection = firestore.collection("turni")
//
//            val turno1 = TurnoDto(
//                id = "", // Firestore lo assegnerà automaticamente
//                nome = "Mattina",
//                idAzienda = "azienda123",
//                idDipendenti = listOf("dip1", "dip2"),
//                orarioInizio = "08:00",
//                orarioFine = "12:00",
//                dipendente = "dip1",
//                dipartimentoId = "dipartimentoA",
//                data = now,
//                note = "Turno mattutino di esempio",
//                isConfermato = true,
//                createdAt = now,
//                updatedAt = now
//            )
//
//            val turno2 = TurnoDto(
//                id = "",
//                nome = "Pomeriggio",
//                idAzienda = "azienda123",
//                idDipendenti = listOf("dip3"),
//                orarioInizio = "14:00",
//                orarioFine = "18:00",
//                dipendente = "dip3",
//                dipartimentoId = "dipartimentoB",
//                data = now,
//                note = "Turno pomeridiano di test",
//                isConfermato = false,
//                createdAt = now,
//                updatedAt = now
//            )
//
//            collection.add(turno1).await()
//            collection.add(turno2).await()
//
//            Log.d("TURNI_DEBUG", "✅ Mock turni creati correttamente")
//            true
//        } catch (e: Exception) {
//            Log.e("TURNI_DEBUG", "❌ Errore durante la creazione dei mock turni", e)
//            false
//        }
        return true
    }


    suspend fun updateTurno(turno: Turno): Resource<String> {
        return try {
            if (turno.id.isEmpty()) {
                Log.e("TURNI_DEBUG", "❌ ID turno mancante, impossibile aggiornare")
                return Resource.Error("ID turno mancante, impossibile aggiornare")
            }

            val turnoMap = turno.toDto() // se hai un mapper dedicato
            // Oppure mappa manualmente: mapOf("nome" to turno.nome, ...)

            firestore.collection(TurniFirestore.COLLECTION)
                .document(turno.id)
                .set(turnoMap)
                .await()

            Log.d("TURNI_DEBUG", "✅ Turno con ID ${turno.id} aggiornato con successo.")
            Resource.Success(turno.id)

        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "❌ Errore durante aggiornamento turno ID ${turno.id}", e)
            Resource.Error("Errore durante l'aggiornamento del turno: ${e.message}")
        }
    }


    suspend fun deleteTurno(turnoId: String): Resource<Boolean> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(turnoId)
                .delete()
                .await()
            Log.d("TURNI_DEBUG", "Turno con id $turnoId eliminato con successo")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "Errore durante eliminazione turno $turnoId", e)
            Resource.Error("Errore durante eliminazione turno: ${e.message}")
        }
    }


    suspend fun getTurniRangeByAzienda(idAzienda: String, startRange: LocalDate, endRange: LocalDate): Resource<List<Turno>> {
        return try {
            val startTimestamp =  startRange.toFirebaseTimestamp()
            val endTimestamp = endRange.toFirebaseTimestamp()

            val result = firestore.collection(TurniFirestore.COLLECTION)
                .whereEqualTo("idAzienda", idAzienda)
                .whereGreaterThanOrEqualTo("data", startTimestamp)
                .whereLessThan("data", endTimestamp)
                .get()
                .await()

            val turni = result.mapNotNull { document ->
                document.toObject(TurnoDto::class.java)?.copy(id = document.id)
            }

            Log.d("TURNI_DEBUG", "🔍 Recuperati ${turni.size} turni per azienda $idAzienda")

            if (turni.isEmpty()) {
                Resource.Empty
            }
            else {
                Resource.Success(turni.toDomainList())
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "❌ Errore durante getTurniByAzienda: ${e.message}")
            Resource.Error("Errore durante il recupero dei turni per l'azienda")
        }
    }


    suspend fun caricaTurni(giornoSelezionato : LocalDate): Resource<List<Turno>> {
        Log.d("TURNI_DEBUG", "SONO nel repository")

        val startOfDay = giornoSelezionato.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val endOfDay = giornoSelezionato.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()

        val startTimeStamp = Timestamp(Date.from(startOfDay))
        val endTimestamp = Timestamp(Date.from(endOfDay))

        return try {
            val result = firestore.collection(TurniFirestore.COLLECTION)
                .whereGreaterThanOrEqualTo(TurniFirestore.Fields.DATA, startTimeStamp)
                .whereLessThan(TurniFirestore.Fields.DATA,endTimestamp)
                .get()
                .await()



            val turni = result.mapNotNull { document ->
                document.toObject(Turno::class.java)?.copy(id = document.id)
            }

            Log.d("TURNI_DEBUG", "Risultati Firestore: ${result.documents.size} documenti")
            Log.d("TURNI_DEBUG", "Turni mappati: ${turni.size}")
            Log.d("TURNI_DEBUG", "Turni: $turni")

            when(turni)
            {
                null -> Resource.Empty
                else -> Resource.Success(turni)
            }
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "Errore nel caricare i turni", e)
            Resource.Error(message = "Errore nel caricare i turni")        }
    }


    suspend fun aggiungiTurno(turno : Turno) : Boolean {
        return try {
            val result =
                firestore.collection(TurniFirestore.COLLECTION)
                    .add(turno)
                    .await()

            val idGenerato = result.id
            Log.d("TURNI_DEBUG", "Turno aggiunto con id" + idGenerato.toString())

            true
        }
        catch (e : Exception)
        {
            Log.e("TURNI_DEBUG", "Errore nell'aggiunta del turno ", e)
            false
        }
    }

}

