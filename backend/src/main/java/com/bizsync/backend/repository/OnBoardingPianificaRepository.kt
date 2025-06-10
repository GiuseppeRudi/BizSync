package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.constantsFirestore.InvitiFirestore
import com.bizsync.backend.hilt.AiModule
import com.bizsync.model.constants.StatusInvite
import com.bizsync.model.domain.AreaLavoro
import com.bizsync.model.domain.Invito
import com.bizsync.model.domain.TurnoFrequente
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class OnBoardingPianificaRepository @Inject constructor(private val db : FirebaseFirestore , private val ai : GenerativeModel  ) {

    suspend fun setTurniAi(nomeAzienda: String): List<AreaLavoro> {
        return try {
            val prompt = """
            Sei un esperto di organizzazione aziendale. 
            
            COMPITO: Genera esattamente 10 aree di lavoro tipiche per un'azienda di tipo "$nomeAzienda".
            
            FORMATO RICHIESTO: Rispondi ESCLUSIVAMENTE con un array JSON valido, senza alcun testo aggiuntivo.
            
            ESEMPIO DI OUTPUT RICHIESTO:
            [
                {"nomeArea": "Reception"},
                {"nomeArea": "Vendite"},
                {"nomeArea": "Magazzino"},
                {"nomeArea": "Amministrazione"},
                {"nomeArea": "Servizio Clienti"},
                {"nomeArea": "Contabilità"},
                {"nomeArea": "Marketing"},
                {"nomeArea": "Risorse Umane"},
                {"nomeArea": "IT Support"},
                {"nomeArea": "Pulizie"}
            ]
            
            IMPORTANTE: Rispondi SOLO con il JSON, nient'altro.
        """.trimIndent()

            val response = ai.generateContent(prompt).text ?: ""
            Log.d("TURNI_AI", "Risposta AI grezza: $response")

            // Pulisci la risposta da eventuali caratteri extra
            val cleanResponse = response.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            Log.d("TURNI_AI", "Risposta AI pulita: $cleanResponse")

            // Parsing con kotlinx.serialization
            val jsonList = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true  // Ignora campi extra
                isLenient = true         // Più permissivo nel parsing
            }.decodeFromString<List<AreaLavoro>>(cleanResponse)

            Log.d("TURNI_AI", "Turni parsati: $jsonList")
            jsonList

        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e("TURNI_AI", "Errore di serializzazione: ${e.message}")
            Log.e("TURNI_AI", "Risposta che ha causato l'errore: ${ai.generateContent("").text}")
            getFallbackTurni(nomeAzienda) // Fallback locale

        } catch (e: Exception) {
            Log.e("TURNI_AI", "Errore generico: ${e.message}")
            getFallbackTurni(nomeAzienda) // Fallback locale
        }
    }


    // 3. Funzione di fallback locale (opzionale ma consigliata)
    private fun getFallbackTurni(nomeAzienda: String): List<AreaLavoro> {
        Log.d("TURNI_AI", "Usando turni di fallback per: $nomeAzienda")
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
}