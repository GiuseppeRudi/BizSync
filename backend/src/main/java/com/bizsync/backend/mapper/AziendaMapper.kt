package com.bizsync.backend.mapper


import com.bizsync.backend.dto.AziendaDto
import com.bizsync.domain.model.Azienda

object AziendaMapper {

    fun toDomain(dto: AziendaDto): Azienda {

        return Azienda(
            idAzienda = dto.id,
            nome = dto.nome,
            areeLavoro = dto.areeLavoro,
            turniFrequenti = dto.turniFrequenti,
            sector = dto.sector,
            numDipendentiRange = dto.numDipendentiRange

        )
    }

    fun toDto(domain: Azienda): AziendaDto {
        return AziendaDto(
            id = domain.idAzienda,
            nome = domain.nome,
            areeLavoro = domain.areeLavoro,
            turniFrequenti = domain.turniFrequenti,
            sector = domain.sector,
            numDipendentiRange = domain.numDipendentiRange
        )
    }
}

// Estensioni
fun AziendaDto.toDomain(): Azienda = AziendaMapper.toDomain(this)
fun Azienda.toDto(): AziendaDto = AziendaMapper.toDto(this)
