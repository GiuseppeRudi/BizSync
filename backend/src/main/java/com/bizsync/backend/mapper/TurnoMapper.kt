package com.bizsync.backend.mapper

import com.bizsync.backend.dto.TurnoDto
import com.bizsync.backend.mapper.PausaMapper.toDomain
import com.bizsync.domain.model.Turno
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
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
            zoneLavorative = dto.zoneLavorative.toZoneLavorativeMap(), // 🆕 Conversione
            pause = dto.pause.toDomainList(),
            note = dto.note.toDomainList(),
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
            zoneLavorative = domain.zoneLavorative.toStringMap(),
            note = domain.note.toDtoList(),
            pause = domain.pause.toDtoList(),
            createdAt = domain.createdAt.toFirebaseTimestamp(),
            updatedAt = domain.updatedAt.toFirebaseTimestamp()
        )
    }

    fun toDomainList(dtoList: List<TurnoDto>): List<Turno> = dtoList.map { toDomain(it) }

    fun toDtoList(domainList: List<Turno>): List<TurnoDto> = domainList.map { toDto(it) }

    // ========== FUNZIONI HELPER PER ZONE LAVORATIVE ==========

    /**
     * Converte Map<String, String> da Firebase in Map<String, ZonaLavorativa>
     */
    private fun Map<String, String>.toZoneLavorativeMap(): Map<String, ZonaLavorativa> {
        return this.mapValues { (_, stringValue) ->
            stringValue.toZonaLavorativa()
        }
    }

    /**
     * Converte Map<String, ZonaLavorativa> in Map<String, String> per Firebase
     */
    private fun Map<String, ZonaLavorativa>.toStringMap(): Map<String, String> {
        return this.mapValues { (_, zonaValue) ->
            zonaValue.name
        }
    }

    /**
     * Converte stringa in ZonaLavorativa con fallback sicuro
     */
    private fun String.toZonaLavorativa(): ZonaLavorativa {
        return try {
            ZonaLavorativa.valueOf(this.uppercase())
        } catch (e: IllegalArgumentException) {
            // Fallback per valori non riconosciuti
            ZonaLavorativa.IN_SEDE
        }
    }
}

// Extension functions per singoli oggetti
fun TurnoDto.toDomain(): Turno = TurnoMapper.toDomain(this)
fun Turno.toDto(): TurnoDto = TurnoMapper.toDto(this)

// Extension functions per liste
fun List<TurnoDto>.toDomainList(): List<Turno> = this.map { it.toDomain() }
fun List<Turno>.toDtoList(): List<TurnoDto> = this.map { it.toDto() }

// ========== UTILITY EXTENSIONS PER ZONE LAVORATIVE ==========

/**
 * Extension per convertire facilmente ZonaLavorativa in stringa
 */
fun ZonaLavorativa.toFirebaseString(): String = this.name

/**
 * Extension per convertire stringa in ZonaLavorativa con fallback
 */
fun String.toZonaLavorativaOrDefault(default: ZonaLavorativa = ZonaLavorativa.IN_SEDE): ZonaLavorativa {
    return try {
        ZonaLavorativa.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        default
    }
}

/**
 * Utility per creare una mappa di zone lavorative con valori di default
 */
fun Map<String, ZonaLavorativa>.withDefaults(dipendentiIds: List<String>): Map<String, ZonaLavorativa> {
    val result = this.toMutableMap()
    dipendentiIds.forEach { id ->
        if (!result.containsKey(id)) {
            result[id] = ZonaLavorativa.IN_SEDE
        }
    }
    return result
}

/**
 * Utility per validare che tutti i dipendenti abbiano una zona assegnata
 */
fun Map<String, ZonaLavorativa>.isCompleteFor(dipendentiIds: List<String>): Boolean {
    return dipendentiIds.all { this.containsKey(it) }
}