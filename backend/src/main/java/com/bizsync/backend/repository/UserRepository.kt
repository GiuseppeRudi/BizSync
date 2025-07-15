package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.UserDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDomainList
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

    suspend fun aggiornaAzienda(idAzienda: String ,idUtente : String, ruolo : RuoliAzienda) : Resource<Unit> {
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

             Resource.Success(Unit)
        }
        catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel aggiungere l'utente", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    suspend fun getDipendentiByAzienda(idAzienda: String): Resource<List<User>> {
        return try {
            Log.d("DIPENDENTI_DEBUG", "Recupero dipendenti per azienda: $idAzienda")

            val result = db.collection(UtentiFirestore.COLLECTION)
                .whereEqualTo(UtentiFirestore.Fields.ID_AZIENDA, idAzienda)
                .get()
                .await()

            Log.d("DIPENDENTI_DEBUG", "Trovati ${result.documents.size} documenti per azienda $idAzienda")

            val dipendenti = result.documents.mapNotNull { document ->
                try {
                    val userDto = document.toObject(UserDto::class.java)?.copy(uid = document.id)
                    Log.d("DIPENDENTI_DEBUG", "Dipendente mappato: ${userDto?.nome} ${userDto?.cognome}")
                    userDto
                } catch (e: Exception) {
                    Log.e("DIPENDENTI_DEBUG", "Errore nel mappare documento ${document.id}", e)
                    null
                }
            }
            Log.d("DIPENDENTI_DEBUG", "Mappati con successo ${dipendenti.size} dipendenti")

            if (dipendenti.isNotEmpty()) {
                Resource.Success(dipendenti.toDomainList())
            } else {
                Log.d("DIPENDENTI_DEBUG", "Nessun dipendente trovato per azienda $idAzienda")
                Resource.Empty
            }

        } catch (e: Exception) {
            Log.e("USER_DEBUG", "Errore nel recupero dipendenti per azienda $idAzienda", e)
            Resource.Error(e.message ?: "Errore nel recupero dipendenti")
        }
    }



    suspend fun updateAcceptInvite(invite: Invito, uid: String): Resource<Unit> {
        return try {
            Log.d("INVITO_DEBUG", invite.toString())

            db.collection(UtentiFirestore.COLLECTION)
                .document(uid)
                .update(
                    UtentiFirestore.Fields.ID_AZIENDA, invite.idAzienda,
                    UtentiFirestore.Fields.MANAGER, invite.manager,
                    UtentiFirestore.Fields.RUOLO, invite.nomeRuolo
                )
                .await()

            Resource.Success(Unit)  // Successo senza dati specifici

        } catch (e: Exception) {
            Log.d("INVITO_DEBUG", e.toString())
            Resource.Error("Errore nell'aggiornamento dell'utente: ${e.message}")
        }
    }





}