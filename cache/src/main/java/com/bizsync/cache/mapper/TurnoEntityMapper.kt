package com.bizsync.cache.mapper

import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno

import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object TurnoEntityMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }



    fun toDomain(entity: TurnoEntity): Turno {
        return Turno(
            id = entity.id,
            idAzienda = entity.idAzienda,
            idDipendenti = entity.idDipendenti,
            idFirebase = entity.idFirebase,
            titolo = entity.titolo,
            data = entity.data,
            orarioInizio = entity.orarioInizio,
            orarioFine = entity.orarioFine,
            dipartimento = entity.dipartimento,
            zoneLavorative = deserializeZoneLavorative(entity.zoneLavorativeJson),
            note = deserializeNote(entity.noteJson),
            pause = deserializePause(entity.pauseJson),
            createdAt = entity.createdAt.toLocalDate(),
            updatedAt = entity.updatedAt.toLocalDate()
        )
    }

    fun toEntity(domain: Turno): TurnoEntity {
        return TurnoEntity(
            id = domain.id,
            idFirebase = domain.idFirebase,
            titolo = domain.titolo,
            idAzienda = domain.idAzienda,
            idDipendenti = domain.idDipendenti,
            data = domain.data,
            orarioInizio = domain.orarioInizio,
            orarioFine = domain.orarioFine,
            dipartimento = domain.dipartimento,
            zoneLavorativeJson = serializeZoneLavorative(domain.zoneLavorative),
            noteJson = serializeNote(domain.note),
            pauseJson = serializePause(domain.pause),
            createdAt = domain.createdAt.toFirebaseTimestamp(),
            updatedAt = domain.updatedAt.toFirebaseTimestamp()
        )
    }

    private fun serializeNote(note: List<Nota>): String {
        return try {
            json.encodeToString(note)
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun deserializeNote(noteJson: String): List<Nota> {
        return try {
            if (noteJson.isBlank()) emptyList()
            else json.decodeFromString<List<Nota>>(noteJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializePause(pause: List<Pausa>): String {
        return try {
            json.encodeToString(pause)
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun deserializePause(pauseJson: String): List<Pausa> {
        return try {
            if (pauseJson.isBlank()) emptyList()
            else json.decodeFromString<List<Pausa>>(pauseJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeZoneLavorative(zone: Map<String, ZonaLavorativa>): String {
        return try {
            json.encodeToString(zone)
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun deserializeZoneLavorative(zoneJson: String): Map<String, ZonaLavorativa> {
        return try {
            if (zoneJson.isBlank()) emptyMap()
            else json.decodeFromString<Map<String, ZonaLavorativa>>(zoneJson)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

// Extension functions
fun TurnoEntity.toDomain(): Turno = TurnoEntityMapper.toDomain(this)
fun Turno.toEntity(): TurnoEntity = TurnoEntityMapper.toEntity(this)
fun List<TurnoEntity>.toDomainList(): List<Turno> = this.map { it.toDomain() }
fun List<Turno>.toEntityList(): List<TurnoEntity> = this.map { it.toEntity() }