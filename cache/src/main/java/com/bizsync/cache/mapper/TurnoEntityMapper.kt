package com.bizsync.cache.mapper

import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno


object TurnoEntityMapper {

    fun toDomain(entity: TurnoEntity): Turno {
        return Turno(
            idDocumento = entity.idDocumento,
            nome = entity.nome,
            giorno = entity.giorno
        )
    }

    fun toEntity(domain: Turno): TurnoEntity {
        return TurnoEntity(
            idDocumento = domain.idDocumento,
            nome = domain.nome,
            giorno = domain.giorno
        )
    }
}

fun TurnoEntity.toDomain(): Turno {
    return TurnoEntityMapper.toDomain(this)
}

fun Turno.toEntity(): TurnoEntity {
    return TurnoEntityMapper.toEntity(this)
}

fun List<TurnoEntity>.toDomainList(): List<Turno> {
    return this.map { it.toDomain() }
}

fun List<Turno>.toEntityList(): List<TurnoEntity> {
    return this.map { it.toEntity() }
}
