package com.bizsync.backend.repository

import android.os.strictmode.InstanceCountViolation
import android.util.Log
import com.bizsync.model.Invito
import com.google.android.recaptcha.internal.zziv
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitoRepository @Inject constructor(private val db : FirebaseFirestore)
{
    suspend fun loadInvito(uid: String): List<Invito> {
        return try {
            val snapshot = db.collection("inviti")
                .whereEqualTo("email", uid) // confronta esattamente con uid
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
            val result = db.collection("inviti")
                .add(invite)
                .await()

            Log.d("INVITI_DEBUG", "Inviti caricati (${result.id}):")
    }


    suspend fun updateInvito(invite : Invito) : Boolean {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())

            db.collection("inviti")
                .document(invite.id)
                .update("stato", "approved")
                .await()

            true
        }
        catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())

            false
        }
    }



}