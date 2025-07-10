package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.prompts.AiPrompts
import com.bizsync.backend.prompts.ContractPrompts
import com.bizsync.backend.remote.ContrattiFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.model.Contratto
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ContractRepository @Inject constructor(
    private val json: Json,
    private val ai: GenerativeModel,
    private val db: FirebaseFirestore
) {

    suspend fun getContrattiByAzienda(idAzienda: String): Resource<List<Contratto>> {
        return try {
            val querySnapshot = db.collection(ContrattiFirestore.COLLECTION)
                .whereEqualTo(ContrattiFirestore.Fields.ID_AZIENDA, idAzienda)
                .get()
                .await()

            val contratti = querySnapshot.documents
                .mapNotNull { it.toObject(Contratto::class.java) }

            Log.d("CONTRATTI_DEBUG", contratti.toString())

            if (contratti.isNotEmpty()) {
                Resource.Success(contratti)
            } else {
                Resource.Empty
            }

        } catch (e: Exception) {
            Resource.Error("Errore nel recupero contratti: ${e.message}")
        }
    }

    suspend fun generateCcnlInfo(
        posizioneLavorativa: String,
        dipartimento: String,
        settoreAziendale: String,
        tipoContratto: String,
        oreSettimanali: String
    ): Ccnlnfo {
        return try {
            val prompt = ContractPrompts.getCcnlInfoPrompt(
                posizioneLavorativa = posizioneLavorativa,
                dipartimento = dipartimento,
                settoreAziendale = settoreAziendale,
                tipoContratto = tipoContratto,
                oreSettimanali = oreSettimanali
            )

            val rawResponse = ai.generateContent(prompt).text.orEmpty()
            Log.d("CCNL_AI", "Risposta AI grezza: $rawResponse")

            val cleanResponse = AiPrompts.cleanAiResponse(rawResponse)
            Log.d("CCNL_AI", "Risposta AI pulita: $cleanResponse")

            val parsed = json.decodeFromString<Ccnlnfo>(cleanResponse)
            Log.d("CCNL_AI", "CCNL info parsata: $parsed")

            parsed

        } catch (e: SerializationException) {
            Log.e("CCNL_AI", "Errore di serializzazione: ${e.message}")
            getFallbackCcnlInfo()
        } catch (e: Exception) {
            Log.e("CCNL_AI", "Errore generico: ${e.message}")
            getFallbackCcnlInfo()
        }
    }

    private fun getFallbackCcnlInfo(): Ccnlnfo {
        Log.d("CCNL_AI", "Usando fallback CCNL info")
        return Ccnlnfo(
            settore = "Commercio",
            ruolo = "Impiegato",
            ferieAnnue = 26,
            rolAnnui = 88,
            stipendioAnnualeLordo = 24000,
            malattiaRetribuita = 180
        )
    }

    suspend fun saveContract(contratto: Contratto): Resource<String> {
        return try {
            val docRef = db.collection(ContrattiFirestore.COLLECTION).document() // genera nuovo ID
            val contrattoConId = contratto.copy(id = docRef.id)

            docRef.set(contrattoConId).await()

            Resource.Success(docRef.id) // restituisci l'ID
        } catch (e: Exception) {
            Resource.Error("Errore nel salvataggio contratto: ${e.message}")
        }
    }

    suspend fun getContrattoByUserAndAzienda(idDipendente: String, idAzienda: String): Resource<Contratto> {
        return try {
            val querySnapshot = db.collection(ContrattiFirestore.COLLECTION)
                .whereEqualTo(ContrattiFirestore.Fields.ID_DIPENDENTE, idDipendente)
                .whereEqualTo(ContrattiFirestore.Fields.ID_AZIENDA, idAzienda)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()

            if (document != null) {
                val contratto = document.toObject(Contratto::class.java)!!
                Resource.Success(contratto)
            } else {
                Resource.Empty
            }

        } catch (e: Exception) {
            Resource.Error("Errore nel recupero contratto: ${e.message}")
        }
    }
}
