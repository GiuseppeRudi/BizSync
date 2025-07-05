package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.prompts.AiPrompts
import com.bizsync.backend.prompts.ContractPrompts
import com.bizsync.domain.model.Ccnlnfo
import com.google.firebase.ai.GenerativeModel
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ContractRepository @Inject constructor(
    private val json: Json,
    private val ai: GenerativeModel
) {

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
}
