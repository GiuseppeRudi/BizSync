package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.Invito
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitoRepository @Inject constructor(private val db : FirebaseFirestore)
{
    suspend fun loadInvito(uid: String): List<Invito> {
        return try {
            val snapshot = db.collection("inviti")
                .whereEqualTo("utente", uid) // confronta esattamente con uid
                .get()
                .await()

// 1) Stampa ogni documento e il suo contenuto
            snapshot.documents.forEach { doc ->
                Log.d("INVITI_DEBUG", "DocID=${doc.id}, data=${doc.data}")
            }

// 2) Dopo averli mappati in Invito
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Invito::class.java)?.copy(id = doc.id)
            }
            Log.d("INVITI_DEBUG", "Inviti caricati (${list.size}): $list")

            list

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("INVITI_DEBUG" , e.toString())
            emptyList()
        }
    }


}