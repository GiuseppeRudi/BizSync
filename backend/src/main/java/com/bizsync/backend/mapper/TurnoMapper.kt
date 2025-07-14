package com.bizsync.backend.mapper

import com.bizsync.backend.dto.TurnoDto
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate
import com.google.firebase.Timestamp

object TurnoMapper {

    fun toDomain(dto: TurnoDto): Turno {
        return Turno(
            id = dto.id,
            nome = dto.nome,
            idAzienda = dto.idAzienda,
            idDipendenti = dto.idDipendenti,
            orarioInizio = dto.orarioInizio,
            orarioFine = dto.orarioFine,
            dipendente = dto.dipendente,
            dipartimentoId = dto.dipartimentoId,
            data = dto.data.toLocalDate(),
            note = dto.note,
            isConfermato = dto.isConfermato,
            createdAt = dto.createdAt.toLocalDate(),
            updatedAt = dto.updatedAt.toLocalDate()
        )
    }

    fun toDto(domain: Turno): TurnoDto {
        return TurnoDto(
            id = domain.id,
            nome = domain.nome,
            idAzienda = domain.idAzienda,
            idDipendenti = domain.idDipendenti,
            orarioInizio = domain.orarioInizio,
            orarioFine = domain.orarioFine,
            dipendente = domain.dipendente,
            dipartimentoId = domain.dipartimentoId,
            data = domain.data.toFirebaseTimestamp(),
            note = domain.note,
            isConfermato = domain.isConfermato,
            createdAt = domain.createdAt.toFirebaseTimestamp(),
            updatedAt = domain.updatedAt.toFirebaseTimestamp()
        )
    }

    fun toDomainList(dtoList: List<TurnoDto>): List<Turno> {
        return dtoList.map { toDomain(it) }
    }

    fun toDtoList(domainList: List<Turno>): List<TurnoDto> {
        return domainList.map { toDto(it) }
    }
}

// Estensioni di comodità
fun TurnoDto.toDomain(): Turno = TurnoMapper.toDomain(this)
fun Turno.toDto(): TurnoDto = TurnoMapper.toDto(this)

fun List<TurnoDto>.toDomainList(): List<Turno> = TurnoMapper.toDomainList(this)
fun List<Turno>.toDtoList(): List<TurnoDto> = TurnoMapper.toDtoList(this)
