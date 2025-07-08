package com.bizsync.cache.mapper

import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno


object TurnoEntityMapper {

    fun fromEntity(entity: TurnoEntity): Turno {
        return Turno(
            idDocumento = entity.idDocumento,
            nome = entity.nome,
            giorno = entity.giorno  // già Timestamp, nessuna conversione
        )
    }

    fun toEntity(domain: Turno): TurnoEntity {
        return TurnoEntity(
            idDocumento = domain.idDocumento,
            nome = domain.nome,
            giorno = domain.giorno  // già Timestamp, nessuna conversione
        )
    }
}
