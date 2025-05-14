package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.Azienda
import com.bizsync.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AziendaRepository @Inject constructor(private val db : FirebaseFirestore) {

    suspend fun creaAzienda(azienda : Azienda) : String? {
         try {
            val result = db.collection("aziende")
                .add(azienda)
                .await()

            val idGenerato = result.id
            Log.d("AZIENDA_DEBUG", "Azienda aggiunta con id" + idGenerato.toString())

             return idGenerato.toString()
        }

        catch (e: Exception) {
            return null
            Log.e("AZIENDA_DEBUG", "Errore nel salvare l'azienda", e)
        }
    }

    suspend fun getAziendaById(aziendaId: String): Azienda? {
        try {
            val result = db.collection("aziende")
                .document(aziendaId)
                .get()
                .await()

            Log.e("AZIENDA_DEBUG", "ho preso l'azienda" + result.toString())
            return result.toObject(Azienda::class.java)?.copy(idAzienda = result.id )

        }

        catch (e: Exception) {
            Log.e("AZINEDA_DEBUG", "Errore nel prendere l'azienda", e)
            return null
        }
    }

}