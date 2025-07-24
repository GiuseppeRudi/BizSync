package com.bizsync.backend.mapper


import com.bizsync.backend.dto.PausaDto
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.constants.enumClass.TipoPausa
import java.time.Duration

object PausaMapper {

    fun PausaDto.toDomain(): Pausa {
        return Pausa(
            id = this.id,
            durata = this.durataMinuti.toDuration(),
            tipo = this.tipo.toTipoPausa(),
            èRetribuita = this.èRetribuita,
            note = this.note
        )
    }


    fun Pausa.toDto(): PausaDto {
        return PausaDto(
            id = this.id,
            durataMinuti = this.durata.toMinutes(),
            tipo = this.tipo.name,
            èRetribuita = this.èRetribuita,
            note = this.note
        )
    }


    fun List<PausaDto>.toDomain(): List<Pausa> {
        return this.map { it.toDomain() }
    }


    fun List<Pausa>.toDto(): List<PausaDto> {
        return this.map { it.toDto() }
    }

    private fun Long.toDuration(): Duration {
        return Duration.ofMinutes(this)
    }


    private fun String.toTipoPausa(): TipoPausa {
        return try {
            TipoPausa.valueOf(this.uppercase())
        } catch (e: IllegalArgumentException) {
            TipoPausa.PAUSA_PRANZO
        }
    }
}

/**
 * Extension function per convertire PausaDto in Pausa
 */
fun PausaDto.toDomain(): Pausa = PausaMapper.run { this@toDomain.toDomain() }

/**
 * Extension function per convertire Pausa in PausaDto
 */
fun Pausa.toDto(): PausaDto = PausaMapper.run { this@toDto.toDto() }

/**
 * Extension function per convertire lista di PausaDto in lista di Pausa
 */
fun List<PausaDto>.toDomainList(): List<Pausa> = PausaMapper.run { this@toDomainList.toDomain() }

/**
 * Extension function per convertire lista di Pausa in lista di PausaDto
 */
fun List<Pausa>.toDtoList(): List<PausaDto> = PausaMapper.run { this@toDtoList.toDto() }