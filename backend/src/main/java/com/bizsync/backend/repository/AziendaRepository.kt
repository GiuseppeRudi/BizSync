package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.AziendaDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.AziendeFirestore
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AziendaRepository @Inject constructor(private val db : FirebaseFirestore) {

    suspend fun creaAzienda(azienda : Azienda) : String? {
         try {

            val aziendaDto = azienda.toDto()

            val result = db.collection(AziendeFirestore.COLLECTION)
                .add(aziendaDto)
                .await()

            val idGenerato = result.id
            Log.d("AZIENDA_DEBUG", "Azienda aggiunta con id" + idGenerato.toString())

             return idGenerato.toString()
        }

        catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel salvare l'azienda", e)
            return null
        }
    }

    suspend fun addPianificaSetup(idAzienda: String, aree: List<AreaLavoro>, turni: List<TurnoFrequente>): Boolean {
        return try {
            db.collection(AziendeFirestore.COLLECTION)
                .document(idAzienda)
                .set(
                    mapOf(
                        AziendeFirestore.Fields.AREE to aree,
                        AziendeFirestore.Fields.TURNI to turni
                    ),
                    SetOptions.merge()
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAziendaById(aziendaId: String): Azienda? {
        try {
            val result = db.collection(AziendeFirestore.COLLECTION)
                .document(aziendaId)
                .get()
                .await()

            Log.e("AZIENDA_DEBUG", "ho preso l'azienda" + result.toString())
            val aziendaDto =  result.toObject(AziendaDto::class.java)?.copy(id = result.id )

            return aziendaDto?.toDomain()

        }

        catch (e: Exception) {
            Log.e("AZINEDA_DEBUG", "Errore nel prendere l'azienda", e)
            return null
        }
    }

}