package com.bizsync.backend.mapper

import com.bizsync.backend.dto.InvitoDto
import com.bizsync.domain.model.Invito
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.google.firebase.Timestamp

object InvitoMapper {

        fun toDomain(dto: InvitoDto): Invito {
            return Invito(
                id = dto.id,
                aziendaNome = dto.aziendaNome,
                email = dto.email,
                idAzienda = dto.idAzienda,
                manager = dto.manager,
                nomeRuolo = dto.nomeRuolo,
                settoreAziendale = dto.settoreAziendale,
                stato = dto.stato,
                ccnlInfo = dto.ccnlInfo,
                sentDate = dto.sentDate.toLocalDate(),
                acceptedDate = if (dto.acceptedDate.seconds == 0L && dto.acceptedDate.nanoseconds == 0) null else dto.acceptedDate.toLocalDate(),
                dipartimento = dto.dipartimento,
                tipoContratto = dto.tipoContratto,
                oreSettimanali = dto.oreSettimanali
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
                ccnlInfo = domain.ccnlInfo,
                sentDate = domain.sentDate.toFirebaseTimestamp(),
                acceptedDate = domain.acceptedDate?.toFirebaseTimestamp() ?: Timestamp(0, 0),
                settoreAziendale = domain.settoreAziendale,
                dipartimento = domain.dipartimento,
                tipoContratto = domain.tipoContratto,
                oreSettimanali = domain.oreSettimanali
            )
        }



    fun toDomainList(dtoList: List<InvitoDto>): List<Invito> {
        return dtoList.map { toDomain(it) }
    }

    fun toDtoList(domainList: List<Invito>): List<InvitoDto> {
        return domainList.map { toDto(it) }
    }
}

fun InvitoDto.toDomain(): Invito = InvitoMapper.toDomain(this)
fun Invito.toDto(): InvitoDto = InvitoMapper.toDto(this)

fun List<InvitoDto>.toDomainList(): List<Invito> = InvitoMapper.toDomainList(this)
fun List<Invito>.toDtoList(): List<InvitoDto> = InvitoMapper.toDtoList(this)
