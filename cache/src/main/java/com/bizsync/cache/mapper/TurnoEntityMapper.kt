package com.bizsync.cache.mapper

import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TurnoEntityMapper {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(entity: TurnoEntity): Turno {
        return Turno(
            id = entity.idDocumento,
            idAzienda = entity.idAzienda,
            idDipendenti = entity.idDipendenti,
            nome = entity.nome,
            data =  entity.data,
            orarioInizio = entity.orarioInizio,
            orarioFine = entity.orarioFine,
            dipendente = entity.dipendente,
            dipartimentoId = entity.dipartimentoId,
            note = entity.note,
            isConfermato = entity.isConfermato,
            createdAt = entity.createdAt.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            updatedAt = entity.updatedAt.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        )
    }

    fun toEntity(domain: Turno): TurnoEntity {
        return TurnoEntity(
            idDocumento = domain.id,
            nome = domain.nome,
            idAzienda = domain.idAzienda,
            idDipendenti = domain.idDipendenti,
            orarioInizio = domain.orarioInizio,
            data = domain.data,
            orarioFine = domain.orarioFine,
            dipendente = domain.dipendente,
            dipartimentoId = domain.dipartimentoId,
            note = domain.note,
            isConfermato = domain.isConfermato,
            createdAt = Timestamp.now(), // o da domain.createdAt se vuoi conservarlo
            updatedAt = Timestamp.now()  // oppure domain.updatedAt
        )
    }
}

fun TurnoEntity.toDomain(): Turno = TurnoEntityMapper.toDomain(this)
fun Turno.toEntity(): TurnoEntity = TurnoEntityMapper.toEntity(this)
fun List<TurnoEntity>.toDomainList(): List<Turno> = this.map { it.toDomain() }
fun List<Turno>.toEntityList(): List<TurnoEntity> = this.map { it.toEntity() }
