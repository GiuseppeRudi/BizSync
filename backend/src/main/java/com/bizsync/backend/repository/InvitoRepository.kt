package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.constantsFirestore.InvitiFirestore
import com.bizsync.model.constants.StatusInvite
import com.bizsync.model.domain.Invito
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitoRepository @Inject constructor(private val db : FirebaseFirestore)
{
    suspend fun loadInvito(email: String): List<Invito> {
        return try {
            val snapshot = db.collection(InvitiFirestore.COLLECTION)
                .whereEqualTo(InvitiFirestore.Fields.EMAIL, email) // confronta esattamente con uid
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                Log.d("INVITI_DEBUG", "DocID=${doc.id}, data=${doc.data}")
            }

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


    suspend fun caricaInvito(invite : Invito)  {

            Log.d("INVITO_DEBUG", invite.toString())
            val result = db.collection(InvitiFirestore.COLLECTION)
                .add(invite)
                .await()

            Log.d("INVITI_DEBUG", "Inviti caricati (${result.id}):")
    }


    suspend fun updateInvito(invite : Invito) : Boolean {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())

            db.collection(InvitiFirestore.COLLECTION)
                .document(invite.id)
                .update(InvitiFirestore.Fields.STATO, StatusInvite.APPROVED)
                .await()

            true
        }
        catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())

            false
        }
    }



}