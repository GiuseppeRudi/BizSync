package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.constantsFirestore.UtentiFirestore
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Invito
import com.bizsync.model.domain.User
import com.bizsync.model.sealedClass.RuoliAzienda
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.temporal.UnsupportedTemporalTypeException
import javax.inject.Inject


class UserRepository @Inject constructor(private val db : FirebaseFirestore) {


    suspend fun getUserById(userId: String): User? {
        try {
            val result = db.collection(UtentiFirestore.COLLECTION)
                .document(userId)
                .get()
                .await()

            Log.e("USER_DEBUG", "ho preso l'utente")
            return result.toObject(User::class.java)?.copy(uid = result.id )

        }

        catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel prendere l'utente", e)
            return null
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