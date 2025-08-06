package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.StatoSettimanaleDipendente
import com.bizsync.domain.model.TurniGeneratiAI
import com.bizsync.domain.model.Turno
import java.time.LocalDate

interface TurnoRemoteRepository {
    suspend fun getTurniRangeByAzienda(
        idAzienda: String,
        startRange: LocalDate,
        endRange: LocalDate,
        idEmployee: String? = null
    ): Resource<List<Turno>>

    suspend fun generateTurni(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        dipendentiDisponibili: DipendentiGiorno,
        statoSettimanale: Map<String, StatoSettimanaleDipendente>,
        turniEsistenti: List<Turno>,
        descrizioneAggiuntiva: String
    ): TurniGeneratiAI

    suspend fun syncTurniInRange(startDate: LocalDate, endDate: LocalDate)
    suspend fun syncAllTurni()

    suspend fun syncTurniForUserInRange(userId: String, idAzienda: String, startDate: LocalDate, endDate: LocalDate)

}