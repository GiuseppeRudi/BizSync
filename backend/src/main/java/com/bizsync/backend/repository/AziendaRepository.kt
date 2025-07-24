package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.AziendaDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.mapper.toDtoList
import com.bizsync.backend.remote.AziendeFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import javax.inject.Inject

class AziendaRepository @Inject constructor(private val db : FirebaseFirestore) {

    suspend fun creaAzienda(azienda: Azienda): Resource<String> {
        return try {
            val aziendaDto = azienda.toDto()

            Log.d("AZIENDA_DEBUG", "Azienda $aziendaDto")

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


    suspend fun updateGiornoPublicazioneTurni(idAzienda: String, nuovoGiorno: DayOfWeek): Resource<Unit> {
        return try {
            val giornoString = nuovoGiorno.name

            db.collection(AziendeFirestore.COLLECTION)
                .document(idAzienda)
                .set(
                    mapOf(
                        AziendeFirestore.Fields.GIORNO_PUBBLICAZIONE_TURNI to giornoString
                    ),
                    SetOptions.merge()
                )
                .await()

            Log.d("AZIENDA_DEBUG", "Giorno pubblicazione aggiornato a: $giornoString per azienda: $idAzienda")
            Resource.Success(Unit)

        } catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nell'aggiornare il giorno di pubblicazione", e)
            Resource.Error(message = "Errore durante l'aggiornamento del giorno di pubblicazione: ${e.message}")
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
                            AziendeFirestore.Fields.AREE to aree.toDtoList(),
                            AziendeFirestore.Fields.TURNI to turni // se `TurnoFrequente` è serializzabile
                        ),
                        SetOptions.merge()
                    )
                    .await()

                Resource.Success(Unit)
            } else {
                Resource.Empty
            }

        } catch (e: Exception) {
            Log.e("AZIENDA ", e.toString())
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
                            AziendeFirestore.Fields.AREE to aree.toDtoList()
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

            Log.e("AZIENDA_DEBUG", "ho preso l'azienda $aziendaDto")

            aziendaDto?.toDomain()?.let { azienda ->
                Log.e("AZIENDA_DEBUG", "ho preso l'azienda $azienda")
                Resource.Success(azienda)
            } ?: Resource.Empty





        } catch (e: Exception) {
            Log.e("AZIENDA_DEBUG", "Errore nel prendere l'azienda", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

}