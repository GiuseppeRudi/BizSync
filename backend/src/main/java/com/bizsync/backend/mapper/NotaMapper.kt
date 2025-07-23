package com.bizsync.backend.mapper


import com.bizsync.backend.dto.NotaDto
import com.bizsync.domain.model.Nota
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate

object NotaMapper {

    /**
     * Converte NotaDto in Nota (domain model)
     */
    fun NotaDto.toDomain(): Nota {
        return Nota(
            id = this.id,
            testo = this.testo,
            tipo = this.tipo.toTipoNota(),
            autore = this.autore,
            createdAt = this.createdAt.toLocalDate(),
            updatedAt = this.updatedAt.toLocalDate()
        )
    }

    /**
     * Converte Nota (domain model) in NotaDto
     */
    fun Nota.toDto(): NotaDto {
        return NotaDto(
            id = this.id,
            testo = this.testo,
            tipo = this.tipo.name,
            autore = this.autore,
            createdAt = this.createdAt.toFirebaseTimestamp(),
            updatedAt = this.updatedAt.toFirebaseTimestamp()
        )
    }

    /**
     * Converte una lista di NotaDto in lista di Nota
     */
    fun List<NotaDto>.toDomain(): List<Nota> {
        return this.map { it.toDomain() }
    }

    /**
     * Converte una lista di Nota in lista di NotaDto
     */
    fun List<Nota>.toDto(): List<NotaDto> {
        return this.map { it.toDto() }
    }

    /**
     * Converte stringa in TipoNota enum con fallback
     */
    private fun String.toTipoNota(): TipoNota {
        return try {
            TipoNota.valueOf(this.uppercase())
        } catch (e: IllegalArgumentException) {
            // Fallback per valori non riconosciuti
            TipoNota.GENERALE
        }
    }
}

// Extension functions per un uso più fluido
/**
 * Extension function per convertire NotaDto in Nota
 */
fun NotaDto.toDomain(): Nota = NotaMapper.run { this@toDomain.toDomain() }

/**
 * Extension function per convertire Nota in NotaDto
 */
fun Nota.toDto(): NotaDto = NotaMapper.run { this@toDto.toDto() }

/**
 * Extension function per convertire lista di NotaDto in lista di Nota
 */
fun List<NotaDto>.toDomainList(): List<Nota> = NotaMapper.run { this@toDomainList.toDomain() }

/**
 * Extension function per convertire lista di Nota in lista di NotaDto
 */
fun List<Nota>.toDtoList(): List<NotaDto> = NotaMapper.run { this@toDtoList.toDto() }