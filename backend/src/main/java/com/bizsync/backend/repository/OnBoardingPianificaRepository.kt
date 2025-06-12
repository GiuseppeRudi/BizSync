package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.prompts.AiPrompts
import com.bizsync.backend.prompts.OnBoardingPianificaPrompts
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.json.Json
import javax.inject.Inject


class OnBoardingPianificaRepository @Inject constructor(private val json : Json , private val db : FirebaseFirestore , private val ai : GenerativeModel  ) {

    suspend fun setAreaAi(nomeAzienda: String): List<AreaLavoro> {
        return try {
            val prompt = OnBoardingPianificaPrompts.getAreaLavoroPrompt(nomeAzienda)

            val response = ai.generateContent(prompt).text ?: ""
            Log.d("TURNI_AI", "Risposta AI grezza: $response")

            // Pulisci la risposta da eventuali caratteri extra
            val cleanResponse = AiPrompts.cleanAiResponse(response)

            Log.d("TURNI_AI", "Risposta AI pulita: $cleanResponse")

            // Parsing con kotlinx.serialization
            val jsonList = json.decodeFromString<List<AreaLavoro>>(cleanResponse)

            Log.d("TURNI_AI", "Turni parsati: $jsonList")
            jsonList

        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e("TURNI_AI", "Errore di serializzazione: ${e.message}")
            Log.e("TURNI_AI", "Risposta che ha causato l'errore: ${ai.generateContent("").text}")
            getFallbackArea() // Fallback locale

        } catch (e: Exception) {
            Log.e("TURNI_AI", "Errore generico: ${e.message}")
            getFallbackArea() // Fallback locale
        }
    }


    // 3. Funzione di fallback locale (opzionale ma consigliata)
    private fun getFallbackArea(): List<AreaLavoro> {
        Log.d("TURNI_AI", "Usando turni di fallback ")
        return listOf(
            AreaLavoro("Reception"),
            AreaLavoro("Amministrazione"),
            AreaLavoro("Servizio Clienti"),
            AreaLavoro("Magazzino"),
            AreaLavoro("Vendite"),
            AreaLavoro("Marketing"),
            AreaLavoro("Risorse Umane"),
            AreaLavoro("IT Support"),
            AreaLavoro("Contabilità"),
            AreaLavoro("Supervisione")
        )
    }

    suspend fun setTurniAi(nomeAzienda: String): List<TurnoFrequente> {
        return try {
            val prompt = OnBoardingPianificaPrompts.getTurniFrequentiPrompt(nomeAzienda)

            val response = ai.generateContent(prompt).text ?: ""
            Log.d("TURNI_AI", "Risposta AI grezza: $response")

            // Pulisci la risposta da eventuali caratteri extra
            val cleanResponse = AiPrompts.cleanAiResponse(response)

            Log.d("TURNI_AI", "Risposta AI pulita: $cleanResponse")

            // Parsing con kotlinx.serialization usando l'injection Json
            val jsonList = json.decodeFromString<List<TurnoFrequente>>(cleanResponse)

            Log.d("TURNI_AI", "Turni parsati: $jsonList")
            jsonList

        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e("TURNI_AI", "Errore di serializzazione: ${e.message}")
            Log.e("TURNI_AI", "Risposta che ha causato l'errore: ${ai.generateContent("").text}")
            getFallbackTurni() // fallback locale per i turni

        } catch (e: Exception) {
            Log.e("TURNI_AI", "Errore generico: ${e.message}")
            getFallbackTurni() // fallback locale per i turni
        }
    }


    fun getFallbackTurni(): List<TurnoFrequente> {
        return listOf(
            TurnoFrequente(nome = "Mattina", oraInizio = "08:00", oraFine = "12:00"),
            TurnoFrequente(nome = "Pomeriggio", oraInizio = "13:00", oraFine = "17:00"),
            TurnoFrequente(nome = "Sera", oraInizio = "18:00", oraFine = "22:00"),
            TurnoFrequente(nome = "Turno Notturno", oraInizio = "22:00", oraFine = "06:00"),
            TurnoFrequente(nome = "Turno 24h", oraInizio = "00:00", oraFine = "24:00"),
            TurnoFrequente(nome = "Turno Ridotto", oraInizio = "09:00", oraFine = "13:00"),
            TurnoFrequente(nome = "Turno Weekend", oraInizio = "10:00", oraFine = "14:00"),
            TurnoFrequente(nome = "Turno Serale", oraInizio = "16:00", oraFine = "20:00")
        )
    }



}