package com.bizsync.backend.mapper

import com.bizsync.backend.dto.TurnoDto
import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Turno
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate

import com.bizsync.domain.utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime

object TurnoMapper {

    fun toDomain(dto: TurnoDto): Turno {
        // Ricava LocalDate da dto.data
        val dataLocalDate = dto.data.toLocalDate()

        // Ricava LocalTime da Timestamp orarioInizio e orarioFine
        val orarioInizioLocalTime = dto.orarioInizio.toLocalDateTime().toLocalTime()
        val orarioFineLocalTime = dto.orarioFine.toLocalDateTime().toLocalTime()

        return Turno(
            id = dto.id,
            titolo = dto.titolo,
            idAzienda = dto.idAzienda,
            idDipendenti = dto.idDipendenti,
            orarioInizio = orarioInizioLocalTime,
            orarioFine = orarioFineLocalTime,
            dipartimentoId = dto.dipartimentoId,
            data = dataLocalDate,
            note = parseNoteFromString(dto.note), // se serve parsare note da String
            createdAt = dto.createdAt.toLocalDate(),
            updatedAt = dto.updatedAt.toLocalDate()
        )
    }

    fun toDto(domain: Turno): TurnoDto {
        val dataTimestamp = domain.data.toFirebaseTimestamp()

        // Combina data + orarioInizio per creare LocalDateTime e poi Timestamp
        val orarioInizioTimestamp = LocalDateTime.of(domain.data, domain.orarioInizio).toFirebaseTimestamp()
        val orarioFineTimestamp = LocalDateTime.of(domain.data, domain.orarioFine).toFirebaseTimestamp()

        return TurnoDto(
            id = domain.id,
            titolo = domain.titolo,
            idAzienda = domain.idAzienda,
            idDipendenti = domain.idDipendenti,
            orarioInizio = orarioInizioTimestamp,
            orarioFine = orarioFineTimestamp,
            dipartimentoId = domain.dipartimentoId,
            data = dataTimestamp,
            note = domain.note.joinToString(separator = ";") { it.toString() }, // esempio conversione note a String
            createdAt = domain.createdAt.toFirebaseTimestamp(),
            updatedAt = domain.updatedAt.toFirebaseTimestamp()
        )
    }

    // Aggiungi se serve una funzione per parsare le note da stringa
    private fun parseNoteFromString(noteString: String): List<Nota> {
        // TODO: Implementa la logica se le note vengono salvage come stringa; altrimenti cambia tipo
        return emptyList()
    }

    fun toDomainList(dtoList: List<TurnoDto>): List<Turno> = dtoList.map { toDomain(it) }

    fun toDtoList(domainList: List<Turno>): List<TurnoDto> = domainList.map { toDto(it) }
}

// Extension functions per singoli oggetti
fun TurnoDto.toDomain(): Turno = TurnoMapper.toDomain(this)
fun Turno.toDto(): TurnoDto = TurnoMapper.toDto(this)

// Extension functions per liste
fun List<TurnoDto>.toDomainList(): List<Turno> = this.map { it.toDomain() }
fun List<Turno>.toDtoList(): List<TurnoDto> = this.map { it.toDto() }
