package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.AziendaDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.AziendeFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AziendaRepository @Inject constructor(private val db : FirebaseFirestore) {

    suspend fun creaAzienda(azienda: Azienda): Resource<String> {
        return try {
            val aziendaDto = azienda.toDto()

            val result = db.collection(AziendeFirestore.COLLECTION)
                .add(aziendaDto)
                .await()

            val idGenerato = result.id
            Log.d("AZIENDA_DEBUG", "Azienda aggiunta con id $idGenerato")

            Resource.Success(idGenerato)
        } catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel salvare l'azienda", e)
            Resource.Error(e.toString())
        }
    }



    suspend fun addPianificaSetup(
        idAzienda: String,
        aree: List<AreaLavoro>,
        turni: List<TurnoFrequente>
    ): Resource<Unit> {
        return try {
            val success = aree.isNotEmpty() && turni.isNotEmpty()

            if (success) {
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
                Resource.Success(Unit)}
            else {

                Resource.Empty
            }

        } catch (e: Exception) {
            Resource.Error(message = "Errore durante l'aggiornamento del setup di pianificazione")
        }
    }


    suspend fun updateAreeLavoro(idAzienda: String, aree: List<AreaLavoro>): Resource<Unit> {
        return try {
            val success = aree.isNotEmpty()

            if (success) {
                db.collection(AziendeFirestore.COLLECTION)
                    .document(idAzienda)
                    .set(
                        mapOf(
                            AziendeFirestore.Fields.AREE to aree
                        ),
                        SetOptions.merge()
                    )
                    .await()
                Resource.Success(Unit)
            } else {
                Resource.Empty
            }
        } catch (e: Exception) {
            Resource.Error(message = "Errore durante l'aggiornamento delle aree di lavoro")
        }
    }


    suspend fun getAziendaById(aziendaId: String): Resource<Azienda> {
        return try {
            val result = db.collection(AziendeFirestore.COLLECTION)
                .document(aziendaId)
                .get()
                .await()

            Log.e("AZIENDA_DEBUG", "ho preso l'azienda $result")
            val aziendaDto = result.toObject(AziendaDto::class.java)?.copy(id = result.id)

            aziendaDto?.toDomain()?.let { azienda ->
                Resource.Success(azienda)
            } ?: Resource.Empty

        } catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel prendere l'azienda", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

}