package com.bizsync.backend.mapper


import com.bizsync.backend.dto.AziendaDto
import com.bizsync.domain.model.Azienda

object AziendaMapper {

    fun toDomain(dto: AziendaDto): Azienda? {
        // Verifica presenza di campi obbligatori
        if (dto.id == null || dto.nome == null) return null

        return Azienda(
            idAzienda = dto.id,
            nome = dto.nome,
            areeLavoro = dto.aree_lavoro ?: emptyList(),
            turniFrequenti = dto.turni_frequenti ?: emptyList()
        )
    }

    fun toDto(domain: Azienda): AziendaDto {
        return AziendaDto(
            id = domain.idAzienda,
            nome = domain.nome,
            aree_lavoro = domain.areeLavoro,
            turni_frequenti = domain.turniFrequenti
        )
    }
}

// Estensioni
fun AziendaDto.toDomain(): Azienda? = AziendaMapper.toDomain(this)
fun Azienda.toDto(): AziendaDto = AziendaMapper.toDto(this)
