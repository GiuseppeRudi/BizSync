package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.InvitiFirestore
import com.bizsync.domain.constants.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
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


    suspend fun caricaInvito(invite: Invito): Resource<Unit> {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())
            val result = db.collection(InvitiFirestore.COLLECTION)
                .add(invite.toDto())
                .await()
            Log.d("INVITI_DEBUG", "Invito caricato con id: ${result.id}")
            Resource.Success(Unit) // ritorno l'invito caricato con successo
        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Errore nel caricamento invito", e)
            Resource.Error("Errore durante il caricamento dell'invito: ${e.message}")
        }
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