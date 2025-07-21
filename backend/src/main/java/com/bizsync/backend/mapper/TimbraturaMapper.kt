package com.bizsync.backend.mapper

import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDateTime



import com.bizsync.backend.dto.TimbraturaDto
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.utils.DateUtils
import java.time.LocalDateTime

object TimbraturaMapper {

    /**
     * Converte da DTO Firebase a Domain Model
     */
    fun toDomain(dto: TimbraturaDto): Timbratura {
        val dtoTs = dto.dataOraTimbratura
        val dataLocal = dtoTs.toLocalDateTime()
        return Timbratura(
            id = dto.idFirebase,
            idTurno = dto.idTurno,
            idDipendente = dto.idDipendente,
            idAzienda = dto.idAzienda,
            idFirebase = dto.idFirebase,

            tipoTimbratura = TipoTimbratura.valueOf(dto.tipoTimbratura),
            dataOraTimbratura = dataLocal,
            dataOraPrevista = dto.dataOraPrevista.toLocalDateTime(),

            zonaLavorativa = ZonaLavorativa.valueOf(dto.zonaLavorativa),

            posizioneVerificata = dto.posizioneVerificata,
            distanzaDallAzienda = dto.distanzaDallAzienda,
            dentroDellaTolleranza = dto.dentroDellaTolleranza,

            statoTimbratura = StatoTimbratura.valueOf(dto.statoTimbratura),
            minutiRitardo = dto.minutiRitardo,

            note = dto.note,
            verificataDaManager = dto.verificataDaManager,

            createdAt = dto.createdAt.toLocalDateTime(),
            updatedAt = dto.updatedAt.toLocalDateTime()
        )
    }

    /**
     * Converte da Domain Model a DTO per Firebase
     */
    fun toDto(model: Timbratura): TimbraturaDto = TimbraturaDto(
        idFirebase = model.idFirebase,
        idTurno = model.idTurno,
        idDipendente = model.idDipendente,
        idAzienda = model.idAzienda,

        tipoTimbratura = model.tipoTimbratura.name,
        dataOraTimbratura = model.dataOraTimbratura.toFirebaseTimestamp(),
        dataOraPrevista = model.dataOraPrevista.toFirebaseTimestamp(),
        timestamp = model.dataOraTimbratura.toFirebaseTimestamp(),

        zonaLavorativa = model.zonaLavorativa.name,

        posizioneVerificata = model.posizioneVerificata,
        distanzaDallAzienda = model.distanzaDallAzienda,
        dentroDellaTolleranza = model.dentroDellaTolleranza,

        statoTimbratura = model.statoTimbratura.name,
        minutiRitardo = model.minutiRitardo,

        note = model.note,
        verificataDaManager = model.verificataDaManager,

        createdAt = model.createdAt.toFirebaseTimestamp(),
        updatedAt = model.updatedAt.toFirebaseTimestamp()
    )

    fun List<TimbraturaDto>.toDomainList(): List<Timbratura> = map { toDomain(it) }
    fun List<Timbratura>.toDtoList(): List<TimbraturaDto> = map { toDto(it) }
}
