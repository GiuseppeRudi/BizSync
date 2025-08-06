package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import java.time.LocalDate

interface TimbraturaRemoteRepository {
    suspend fun getTimbratureByAzienda(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Timbratura>>

    suspend fun verificaTimbratura(idTimbratura: String): Resource<Unit>

    suspend fun syncTimbratureForUserInRange(userId: String, idAzienda: String, startDate: LocalDate, endDate: LocalDate)

}
