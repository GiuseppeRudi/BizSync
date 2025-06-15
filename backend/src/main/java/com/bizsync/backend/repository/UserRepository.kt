package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.UserDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.remote.UtentiFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.model.User
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class UserRepository @Inject constructor(private val db : FirebaseFirestore) {


    suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val result = db.collection(UtentiFirestore.COLLECTION)
                .document(userId)
                .get()
                .await()

            Log.e("USER_DEBUG", "ho preso l'utente")
            val userDto = result.toObject(UserDto::class.java)?.copy(uid = result.id )

            Log.e("USER_DEBUG", "utente dto $userDto")

            userDto?.toDomain()?.let { user ->
                Resource.Success(user)
            } ?: Resource.Empty

        }

        catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel prendere l'utente", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    suspend fun addUser(user: User,uid : String) : Boolean {
         return try {
             db.collection(UtentiFirestore.COLLECTION)
                .document(uid)
                .set(user)
                .await()

             Log.e("USER_DEBUG", "CARICAMENTO UTENTE ESEGUITO CON SUCCESSO")
             true
        }
        catch (e: Exception) {

            Log.e("USER_DEBUG", "Errore nel aggiungere l'utente", e)
            false
        }
    }

    suspend fun aggiornaAzienda(idAzienda: String ,idUtente : String, ruolo : RuoliAzienda) : Boolean {
        Log.d("AZIENDA_DEBUG", "SONO NEL REPOSITORY E ID UTENTE " + idUtente.toString())
        Log.d("AZIENDA_DEBUG", "SONO NEL REPOSITORY E ID AZIENDA " + idAzienda.toString())

        val updates = mapOf(
            UtentiFirestore.Fields.ID_AZIENDA to idAzienda,
            UtentiFirestore.Fields.RUOLO      to ruolo.route,
            UtentiFirestore.Fields.MANAGER    to ruolo.isPrivileged
        )

        return try {
            val result = db.collection(UtentiFirestore.COLLECTION)
                .document(idUtente)
                .update(updates)
                .await()

            true
        }
        catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel aggiungere l'utente", e)
            false
        }
    }

    suspend fun checkUser(idUtente : String) : Boolean {
        return try {
            val result = db.collection(UtentiFirestore.COLLECTION)
                .document(idUtente)
                .get()
                .await()

             result.exists()
        }
        catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel aggiungere l'utente", e)
            false
        }
    }




    suspend fun ottieniIdAzienda(idUtente: String): String? {
        return try {
            val document = db.collection(UtentiFirestore.COLLECTION)
                .document(idUtente)
                .get()
                .await()

            val idAzienda = document.getString(UtentiFirestore.Fields.ID_AZIENDA)
            Log.d("USER_DEBUG", "ID Azienda: $idAzienda")
            idAzienda
        } catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nell'ottenere l'idAzienda", e)
            null
        }
    }

    suspend fun updateAcceptInvite(invite : Invito, uid : String) : Boolean {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())

            db.collection(UtentiFirestore.COLLECTION)
                .document(uid)
                .update(
                    UtentiFirestore.Fields.ID_AZIENDA, invite.azienda,
                    UtentiFirestore.Fields.MANAGER, invite.manager,
                    UtentiFirestore.Fields.RUOLO, invite.nomeRuolo
                )
                .await()
            true
        }
        catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())

            false
        }
    }




}