package com.bizsync.backend.repository


import android.util.Log
import com.bizsync.backend.dto.AbsenceDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.mapper.toDomainList
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.remote.AbsencesFirestore
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.mapper.toEntity
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceRemoteRepository
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class AbsenceRemoteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val absenceDao : AbsenceDao)  : AbsenceRemoteRepository {


    suspend fun checkAbsenceChangesInWindow(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate,
        idEmployee: String?
    ): Resource<List<Absence>> {
        return try {
            Log.d("ABSENCE_CHECK", " Check completo assenze dal $startDate al $endDate")

            val startTimestamp = startDate.toFirebaseTimestamp()
            val endTimestamp = endDate.atTime(LocalTime.MAX).toFirebaseTimestamp()

            var query = db.collection(AbsencesFirestore.COLLECTION)
                .whereEqualTo(AbsencesFirestore.Fields.IDAZIENDA, idAzienda)
                .whereGreaterThanOrEqualTo(AbsencesFirestore.Fields.START_DATE_TIME, startTimestamp)
                .whereLessThanOrEqualTo(AbsencesFirestore.Fields.END_DATE_TIME, endTimestamp)

            // Aggiunge il filtro per idUtente solo se idEmployee è non nullo
            if (idEmployee != null) {
                query = query.whereEqualTo(AbsencesFirestore.Fields.IDUSER, idEmployee)
            }

            val querySnapshot = query.get().await()

            val absencesDto = querySnapshot.documents
                .mapNotNull { it.toObject(AbsenceDto::class.java) }

            val absences = absencesDto.toDomainList()

            if (absences.isNotEmpty()) {
                Resource.Success(absences)
            } else {
                Resource.Empty
            }

        } catch (e: Exception) {
            Log.e("ABSENCE_CHECK", " Errore check completo: ${e.message}")
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }


    override suspend fun syncAbsencesInRange(startDate: LocalDate, endDate: LocalDate) {
        try {
            val absencesFromFirebase = db.collection("absences")
                .whereGreaterThanOrEqualTo("startDate", startDate.toString())
                .whereLessThanOrEqualTo("endDate", endDate.toString())
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<Absence>()?.copy(id = doc.id)
                }

            absencesFromFirebase.forEach { absence ->
                absenceDao.insert(absence.toEntity())
            }

        } catch (e: Exception) {
            throw Exception("Errore nel sync assenze: ${e.message}")
        }
    }

    override suspend fun syncAllAbsences() {
        try {
            val allAbsences = db.collection("absences")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<Absence>()?.copy(id = doc.id)
                }

            allAbsences.forEach { absence ->
                absenceDao.insert(absence.toEntity())
            }

        } catch (e: Exception) {
            throw Exception("Errore nel sync completo assenze: ${e.message}")
        }
    }

    override suspend fun salvaAbsence(absence: Absence): Resource<String> {
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

    override suspend fun getAllAbsences(idUser: String): Resource<List<Absence>> {
        return try {
            val snapshot = db.collection(AbsencesFirestore.COLLECTION)
                .whereEqualTo(AbsencesFirestore.Fields.IDUSER, idUser)
                .get()
                .await()

            val absences = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AbsenceDto::class.java)?.copy(id = doc.id)?.toDomain()
            }

            if (absences.isNotEmpty()) {
                Resource.Success(absences)
            } else {
                Resource.Empty
            }

        } catch (e: Exception) {
            Log.e("ABSENCE_DEBUG", "Errore durante il recupero delle assenze", e)
            Resource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    override suspend fun getAllAbsencesByAzienda(idAzienda: String): Resource<List<Absence>> {
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


    override suspend fun updateAbsence(absence: Absence): Resource<Unit> {
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

