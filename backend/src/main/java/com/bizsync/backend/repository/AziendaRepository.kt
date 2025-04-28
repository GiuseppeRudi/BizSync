package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.Azienda
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AziendaRepository @Inject constructor(private val db : FirebaseFirestore) {

    suspend fun creaAzienda(azienda : Azienda) : String? {
        try {
            val result = db.collection("aziende")
                .add(azienda)
                .await()

            return result.id
            Log.d("AZIENDA_DEBUG", "Azienda aggiunta con id" + result.id.toString())

        }

        catch (e: Exception) {

            // DA SISTEMARE
            return null
            Log.e("AZIENDA_DEBUG", "Errore nel salvare l'azienda", e)
        }
    }

}