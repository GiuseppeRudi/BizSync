package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.model.User
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class UserRepository @Inject constructor(private val db : FirebaseFirestore) {


    suspend fun getUserById(userId: String): User? {
        try {
            val result = db.collection("utenti")
                .document(userId)
                .get()
                .await()

            return result.toObject(User::class.java)

        }

        catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel prendere l'utente", e)
            return null
        }
    }



}