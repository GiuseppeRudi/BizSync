package com.bizsync.backend.repository

import com.google.firebase.ai.GenerativeModel


import android.util.Log
import com.bizsync.backend.prompts.AiPrompts
import com.bizsync.backend.prompts.TurniPrompts
import com.bizsync.domain.model.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class TurniAIRepository @Inject constructor(
    private val ai: GenerativeModel,
    private val json: Json
) {

    suspend fun generateTurni(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        dipendentiDisponibili: DipendentiGiorno,
        statoSettimanale: Map<String, StatoSettimanaleDipendente>,
        turniEsistenti: List<Turno>,
        descrizioneAggiuntiva: String = ""
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
}
