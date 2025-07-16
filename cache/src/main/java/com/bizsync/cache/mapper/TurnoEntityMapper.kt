package com.bizsync.cache.mapper

import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno
import com.google.firebase.Timestamp
import java.time.format.DateTimeFormatter

object TurnoEntityMapper {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(entity: TurnoEntity): Turno {
        return Turno(
            id = entity.id,
            idAzienda = entity.idAzienda,
            idDipendenti = entity.idDipendenti,
            idFirebase = entity.idFirebase,
            titolo = entity.titolo,
            data =  entity.data,
            orarioInizio = entity.orarioInizio,
            orarioFine = entity.orarioFine,
            dipartimentoId = entity.dipartimentoId,
//            note = entity.note,
            createdAt = entity.createdAt.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            updatedAt = entity.updatedAt.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        )
    }

    fun toEntity(domain: Turno): TurnoEntity {
        return TurnoEntity(
            id = domain.id,
            idFirebase = domain.idFirebase,
            titolo = domain.titolo,
            idAzienda = domain.idAzienda,
            idDipendenti = domain.idDipendenti,
            orarioInizio = domain.orarioInizio,
            data = domain.data,
            orarioFine = domain.orarioFine,
            dipartimentoId = domain.dipartimentoId,
            note = "",
            createdAt = Timestamp.now(), // o da domain.createdAt se vuoi conservarlo
            updatedAt = Timestamp.now()  // oppure domain.updatedAt
        )
    }
}

fun TurnoEntity.toDomain(): Turno = TurnoEntityMapper.toDomain(this)
fun Turno.toEntity(): TurnoEntity = TurnoEntityMapper.toEntity(this)
fun List<TurnoEntity>.toDomainList(): List<Turno> = this.map { it.toDomain() }
fun List<Turno>.toEntityList(): List<TurnoEntity> = this.map { it.toEntity() }
