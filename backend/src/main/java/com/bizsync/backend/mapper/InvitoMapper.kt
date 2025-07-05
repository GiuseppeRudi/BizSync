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
            stato = dto.stato ,
            ccnlInfo = dto.ccnlInfo

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
            stato = domain.stato,
            ccnlInfo = domain.ccnlInfo
        )
    }
}

fun InvitoDto.toDomain(): Invito = InvitoMapper.toDomain(this)
fun Invito.toDto(): InvitoDto = InvitoMapper.toDto(this)
