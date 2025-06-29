package com.bizsync.backend.repository


import android.util.Log
import com.bizsync.backend.dto.AbsenceDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.AbsencesFirestore
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AbsenceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun salvaAbsence(absence: Absence): Resource<String> {
        return try {
            val dto = absence.toDto()

            val result = db.collection(AbsencesFirestore.COLLECTION)
                .add(dto)
                .await()

            val id = result.id
            Log.e("ABSENCE_DEBUG", "Funziona")
            Resource.Success(id)
        } catch (e: Exception) {
            Log.e("ABSENCE_DEBUG", "Errore durante il salvataggio dell'assenza", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    suspend fun getAllAbsences(idUser: String): Resource<List<Absence>> {
        return try {
            val snapshot = db.collection(AbsencesFirestore.COLLECTION)
                .whereEqualTo(AbsencesFirestore.Fields.IDUSER, idUser)
                .get()
                .await()

            val absences = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AbsenceDto::class.java)?.copy(id = doc.id)?.toDomain()
            }

            if (absences.isNotEmpty()) Resource.Success(absences)
            else Resource.Empty

        } catch (e: Exception) {
            Log.e("ABSENCE_DEBUG", "Errore durante il recupero delle assenze", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    suspend fun getAllAbsencesByAzienda(idAzienda: String): Resource<List<Absence>> {
        return try {
            val snapshot = db.collection(AbsencesFirestore.COLLECTION)
                .whereEqualTo(AbsencesFirestore.Fields.IDAZIENDA, idAzienda)
                .get()
                .await()

            val absences = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AbsenceDto::class.java)?.copy(id = doc.id)?.toDomain()
            }

            if (absences.isNotEmpty()) Resource.Success(absences)
            else Resource.Empty

        } catch (e: Exception) {
            Log.e("ABSENCE_DEBUG", "Errore durante il recupero delle assenze per azienda", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }


    suspend fun updateAbsence(absence: Absence): Resource<Unit> {
        return try {
            db.collection(AbsencesFirestore.COLLECTION)
                .document(absence.id)
                .set(absence.toDto())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ABSENCE_DEBUG", "Errore aggiornamento assenza", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }


}

