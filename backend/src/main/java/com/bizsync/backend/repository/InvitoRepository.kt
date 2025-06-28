package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.InvitiFirestore
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitoRepository @Inject constructor(private val db : FirebaseFirestore)
{
    suspend fun loadInvito(email: String): Resource<List<Invito>> {
        return try {
            val snapshot = db.collection(InvitiFirestore.COLLECTION)
                .whereEqualTo(InvitiFirestore.Fields.EMAIL, email)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Invito::class.java)?.copy(id = doc.id)
            }

            if (list.isEmpty()) {
                Resource.Empty
            } else {
                Resource.Success(list)
            }

        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Errore durante il caricamento inviti", e)
            Resource.Error("Errore durante il caricamento degli inviti: ${e.localizedMessage}")
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


    suspend fun updateInvito(invite: Invito): Resource<Unit> {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())

            db.collection(InvitiFirestore.COLLECTION)
                .document(invite.id)
                .update(InvitiFirestore.Fields.STATO, StatusInvite.APPROVED)
                .await()

            Resource.Success(Unit)  // Successo senza dati specifici

        } catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())
            Resource.Error("Errore nell'aggiornamento dell'invito: ${e.message}")
        }
    }



}