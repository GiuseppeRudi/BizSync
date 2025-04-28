package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.Azienda
import com.bizsync.model.User
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

    suspend fun addUser(user: User,uid : String) {
         try {
            val result = db.collection("utenti")
                .document(uid)
                .set(user)
                .await()

             Log.e("USER_DEBUG", "CARICAMENTO UTENTE ESEGUITO CON SUCCESSO")

        }
        catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel aggiungere l'utente", e)
        }
    }

    suspend fun aggiornaAzienda(idAzienda: String ,idUtente : String) {
        Log.d("AZIENDA_DEBUG", "SONO NEL REPOSITORY E ID UTENTE " + idUtente.toString())
        Log.d("AZIENDA_DEBUG", "SONO NEL REPOSITORY E ID AZIENDA " + idAzienda.toString())

        try {
            val result = db.collection("utenti")
                .document(idUtente)
                .update("idAzienda",idAzienda)
                .await()


        }
        catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel aggiungere l'utente", e)
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




}