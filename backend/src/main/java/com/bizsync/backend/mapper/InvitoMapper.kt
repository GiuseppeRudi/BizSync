package com.bizsync.backend.mapper

import com.bizsync.backend.dto.InvitoDto
import com.bizsync.domain.model.Invito

object InvitoMapper {

    fun toDomain(dto: InvitoDto): Invito {
        return Invito(
            id = dto.id,
            aziendaNome = dto.aziendaNome,
            email = dto.email,
            idAzienda = dto.idAzienda,
            manager = dto.manager,
            nomeRuolo = dto.nomeRuolo,
            stato = dto.stato  // stringa come arriva da Firebase
        )
    }

    fun toDto(domain: Invito): InvitoDto {
        return InvitoDto(
            id = domain.id,
            aziendaNome = domain.aziendaNome,
            email = domain.email,
            idAzienda = domain.idAzienda,
            manager = domain.manager,
            nomeRuolo = domain.nomeRuolo,
            stato = domain.stato // stato come stringa
        )
    }
}

fun InvitoDto.toDomain(): Invito = InvitoMapper.toDomain(this)
fun Invito.toDto(): InvitoDto = InvitoMapper.toDto(this)
