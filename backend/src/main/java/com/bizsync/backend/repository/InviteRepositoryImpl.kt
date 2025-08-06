package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.InvitoDto
import com.bizsync.backend.mapper.toDomainList
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.InvitiFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.repository.InviteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InviteRepositoryImpl @Inject constructor(private val db : FirebaseFirestore) : InviteRepository
{
    override suspend fun getInvitesByEmail(email: String): Resource<List<Invito>> {
        return try {
            Log.d("INVITI_DEBUG", email)

            val snapshot = db.collection(InvitiFirestore.COLLECTION)
                .whereEqualTo(InvitiFirestore.Fields.EMAIL, email)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(InvitoDto::class.java)?.copy(id = doc.id)
            }

            Log.d("INVITI_DEBUG", list.toString())


            if (list.isEmpty()) {
                Resource.Empty
            } else {
                Resource.Success(list.toDomainList())
            }

        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Errore durante il caricamento inviti", e)
            Resource.Error("Errore durante il caricamento degli inviti: ${e.localizedMessage}")
        }
    }


    override suspend fun getInvitesByAzienda(idAzienda: String): Resource<List<Invito>> {
        return try {

            Log.d("INVITI_DEBUG", "SONOQUA $idAzienda")

            val snapshot = db.collection(InvitiFirestore.COLLECTION)
                .whereEqualTo(InvitiFirestore.Fields.ID_AZIENDA, idAzienda)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(InvitoDto::class.java)?.copy(id = doc.id)
            }

            if (list.isEmpty()) {
                Resource.Empty
            } else {
                Resource.Success(list.toDomainList())
            }

        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Errore durante il caricamento inviti per azienda", e)
            Resource.Error("Errore durante il caricamento: ${e.localizedMessage}")
        }
    }


    override suspend fun caricaInvito(invite: Invito): Resource<Unit> {
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


    override suspend fun updateInvito(invite: Invito): Resource<Unit> {
        return try {


            val inviteDto = invite.toDto()

            db.collection(InvitiFirestore.COLLECTION)
                .document(inviteDto.id)
                .update(
                    InvitiFirestore.Fields.STATO, inviteDto.stato,
                    InvitiFirestore.Fields.ACCEPTED_DATE, inviteDto.acceptedDate
                )
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())
            Resource.Error("Errore nell'aggiornamento dell'invito: ${e.message}")
        }
    }

}