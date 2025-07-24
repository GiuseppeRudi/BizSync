package com.bizsync.backend.mapper


import com.bizsync.backend.dto.NotaDto
import com.bizsync.domain.model.Nota
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate

object NotaMapper {


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


    fun List<NotaDto>.toDomain(): List<Nota> {
        return this.map { it.toDomain() }
    }


    fun List<Nota>.toDto(): List<NotaDto> {
        return this.map { it.toDto() }
    }


    private fun String.toTipoNota(): TipoNota {
        return try {
            TipoNota.valueOf(this.uppercase())
        } catch (e: IllegalArgumentException) {
            TipoNota.GENERALE
        }
    }
}


fun NotaDto.toDomain(): Nota = NotaMapper.run { this@toDomain.toDomain() }
fun Nota.toDto(): NotaDto = NotaMapper.run { this@toDto.toDto() }
fun List<NotaDto>.toDomainList(): List<Nota> = NotaMapper.run { this@toDomainList.toDomain() }
fun List<Nota>.toDtoList(): List<NotaDto> = NotaMapper.run { this@toDtoList.toDto() }