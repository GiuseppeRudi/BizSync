package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.remote.TurniFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class TurnoRepository @Inject constructor(private val db: FirebaseFirestore) {

    suspend fun caricaTurni(giornoSelezionato : LocalDate): Resource<List<Turno>> {
        Log.d("TURNI_DEBUG", "SONO nel repository")

        val startOfDay = giornoSelezionato.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val endOfDay = giornoSelezionato.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()

        val startTimeStamp = Timestamp(Date.from(startOfDay))
        val endTimestamp = Timestamp(Date.from(endOfDay))

        return try {
            val result = db.collection(TurniFirestore.COLLECTION)
                .whereGreaterThanOrEqualTo(TurniFirestore.Fields.DATA, startTimeStamp)
                .whereLessThan(TurniFirestore.Fields.DATA,endTimestamp)
                .get()
                .await()



            val turni = result.mapNotNull { document ->
                val turno = document.toObject(Turno::class.java)
                turno.idDocumento = document.id
                turno
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
                db.collection(TurniFirestore.COLLECTION)
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
