package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.Turno
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class TurnoRepository @Inject constructor(private val db: FirebaseFirestore) {

    suspend fun caricaTurni(giornoSelezionato : LocalDate): List<Turno> {
        Log.d("TURNI_DEBUG", "SONO nel repository")

        val startOfDay = giornoSelezionato.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val endOfDay = giornoSelezionato.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()

        val startTimeStamp = Timestamp(Date.from(startOfDay))
        val endTimestamp = Timestamp(Date.from(endOfDay))

        return try {
            val result = db.collection("turni")
                .whereGreaterThanOrEqualTo("data", startTimeStamp)
                .whereLessThan("data",endTimestamp)
                .get()
                .await()

            if (result.isEmpty) {
                Log.d("TURNI_DEBUG", "Nessun dato trovato nella collezione 'turni'")
            } else {
                Log.d("TURNI_DEBUG", "Dati trovati nella collezione 'turni'")
            }

            // Mappa i risultati in oggetti Turno e assegna l'id del documento
            val turni = result.mapNotNull { document ->
                val turno = document.toObject(Turno::class.java)
                turno.idDocumento = document.id  // 🔥 qui salvi l'ID del documento
                turno
            }

            Log.d("TURNI_DEBUG", "Risultati Firestore: ${result.documents.size} documenti")
            Log.d("TURNI_DEBUG", "Turni mappati: ${turni.size}")
            Log.d("TURNI_DEBUG", "Turni: $turni")

            turni
        } catch (e: Exception) {
            Log.e("TURNI_DEBUG", "Errore nel caricare i turni", e)
            emptyList()
        }
    }

    suspend fun aggiungiTurno(turno : Turno) {
         try {
            val result = db.collection("turni").add(turno).await()

            val idGenerato = result.id
            Log.d("TURNI_DEBUG", "Turno aggiunto con id" + idGenerato.toString())

        }
        catch (e : Exception)
        {
            Log.e("TURNI_DEBUG", "Errore nell'aggiunta del turno ", e)
        }
    }


}
