package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Invito
import com.bizsync.model.domain.User
import com.bizsync.model.sealedClass.RuoliAzienda
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class UserRepository @Inject constructor(private val db : FirebaseFirestore) {


    suspend fun getUserById(userId: String): User? {
        try {
            val result = db.collection("utenti")
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
             db.collection("utenti")
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
            "idAzienda" to idAzienda,
            "ruolo" to ruolo.route,
            "manager" to ruolo.isPrivileged
        )

        return try {
            val result = db.collection("utenti")
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
            val result = db.collection("utenti")
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
            val document = db.collection("utenti")
                .document(idUtente)
                .get()
                .await()

            val idAzienda = document.getString("idAzienda")
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

            db.collection("utenti")
                .document(uid)
                .update(
                    "idAzienda", invite.azienda,
                    "manager", invite.manager,
                    "ruolo", invite.nomeRuolo
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