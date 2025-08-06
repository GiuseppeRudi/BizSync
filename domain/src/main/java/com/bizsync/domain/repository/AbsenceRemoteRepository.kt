package com.bizsync.domain.repository

import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import java.time.LocalDate

interface AbsenceRemoteRepository {
    suspend fun salvaAbsence(absence: Absence): Resource<String>
    suspend fun getAllAbsences(idUser: String): Resource<List<Absence>>
    suspend fun getAllAbsencesByAzienda(idAzienda: String): Resource<List<Absence>>
    suspend fun updateAbsence(absence: Absence): Resource<Unit>

    suspend fun syncAbsencesInRange(startDate: LocalDate, endDate: LocalDate)
    suspend fun syncAllAbsences()
}