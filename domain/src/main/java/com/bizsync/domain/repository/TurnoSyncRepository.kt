package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import java.time.LocalDate

interface TurnoSyncRepository {
    suspend fun getTurni(idAzienda: String, idEmployee: String?, forceRefresh: Boolean = false): Resource<List<Turno>>
    suspend fun deleteOldCachedData(currentDate: LocalDate = LocalDate.now())

    suspend fun fetchTurniSettimana(
        startWeek: LocalDate,
        idAzienda: String? = null,
        idUser: String? = null
    ) :  Resource<List<Turno>>

    suspend fun syncTurniToFirebase(weekStart: LocalDate): Resource<String>

    suspend fun deleteTurno(turnoId: String): Resource<String>

    suspend fun saveTurno(
        turno: Turno,
        dipartimento: String,
        giornoSelezionato: LocalDate,
        idAzienda: String
    ): Resource<String>


}