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
import com.bizsync.backend.prompts.AiPrompts
import com.bizsync.backend.prompts.TurniPrompts
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toEntity
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.StatoSettimanaleDipendente
import com.bizsync.domain.model.TurniGeneratiAI
import com.bizsync.domain.repository.TurnoRemoteRepository
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.firestore.toObject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnoRemoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val json: Json,
    private val ai: GenerativeModel,
    private val turnoDao : TurnoDao
)   : TurnoRemoteRepository{


    override suspend fun generateTurni(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        dipendentiDisponibili: DipendentiGiorno,
        statoSettimanale: Map<String, StatoSettimanaleDipendente>,
        turniEsistenti: List<Turno>,
        descrizioneAggiuntiva: String
    ): TurniGeneratiAI {
        return try {
            val prompt = TurniPrompts.getGeneraTurniPrompt(
                dipartimento = dipartimento,
                giornoSelezionato = giornoSelezionato,
                dipendentiDisponibili = dipendentiDisponibili,
                statoSettimanale = statoSettimanale,
                turniEsistenti = turniEsistenti,
                descrizioneAggiuntiva = descrizioneAggiuntiva
            )

            Log.d("TURNI_AI", "Invio prompt: ${prompt.take(500)}...")

            val rawResponse = ai.generateContent(prompt).text.orEmpty()
            Log.d("TURNI_AI", "Risposta AI grezza: $rawResponse")

            val cleanResponse = AiPrompts.cleanAiResponse(rawResponse)
            Log.d("TURNI_AI", "Risposta AI pulita: $cleanResponse")

            val parsed = json.decodeFromString<TurniGeneratiAI>(cleanResponse)
            Log.d("TURNI_AI", "Turni generati: ${parsed.turniGenerati.size}")

            parsed

        } catch (e: SerializationException) {
            Log.e("TURNI_AI", "Errore di serializzazione: ${e.message}")
            getFallbackTurni()
        } catch (e: Exception) {
            Log.e("TURNI_AI", "Errore generico: ${e.message}")
            getFallbackTurni()
        }
    }

    private fun getFallbackTurni(): TurniGeneratiAI {
        Log.d("TURNI_AI", "Usando fallback turni")
        return TurniGeneratiAI(
            turniGenerati = emptyList(),
            coperturaTotale = false,
            motivoCoperturaParziale = "Errore nella generazione automatica dei turni"
        )
    }


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




    override suspend fun syncTurniInRange(startDate: LocalDate, endDate: LocalDate) {
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

    override suspend fun syncAllTurni() {
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

    override suspend fun syncTurniForUserInRange(
        userId: String,
        aziendaId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        try {
            Log.d("TurnoRemoteRepositoryImpl", "Sincronizzazione turni per utente $userId dal $startDate al $endDate")

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
                        Log.e("TurnoRemoteRepositoryImpl", "Errore conversione turno ${doc.id}: ${e.message}")
                        null
                    }
                }

            Log.d("TurnoRemoteRepositoryImpl", "Trovati ${turniFromFirebase.size} turni su Firebase")

            // Salva tutti i turni in cache locale
            turniFromFirebase.forEach { turno ->
                try {
                    turnoDao.insertTurno(turno.toEntity())
                    Log.d("TurnoRemoteRepositoryImpl", "Turno ${turno.id} salvato in cache")
                } catch (e: Exception) {
                    Log.e("TurnoRemoteRepositoryImpl", "Errore salvataggio turno ${turno.id}: ${e.message}")
                }
            }

            Log.d("TurnoRemoteRepositoryImpl", "Sincronizzazione turni completata")

        } catch (e: Exception) {
            Log.e("TurnoRemoteRepositoryImpl", "Errore nella sincronizzazione turni: ${e.message}")
            throw Exception("Errore nel sincronizzare i turni da Firebase: ${e.message}")
        }
    }



    override suspend fun getTurniRangeByAzienda(
        idAzienda: String,
        startRange: LocalDate,
        endRange: LocalDate,
        idEmployee: String?
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

    override suspend fun getTurnoById(firebaseId: String): Resource<Turno> {
        return try {
            val doc = firestore.collection("turni")
                .document(firebaseId)
                .get()
                .await()

            if (doc.exists()) {
                val turno = doc.toObject(Turno::class.java)
                turno?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Errore conversione turno")
            } else {
                Resource.Empty
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore recupero turno")
        }
    }
}

