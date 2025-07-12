package com.bizsync.backend.hash.extensions

import com.bizsync.backend.dto.AbsenceDto
import com.bizsync.backend.hash.HashManager
import com.bizsync.backend.mapper.toDomain
import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.domain.model.Absence
import com.bizsync.cache.mapper.toDomain

// FUNZIONE HASH SEMPLICE PER DOMAIN ABSENCE
fun Absence.generateAbsenceHash(): String {
    val absenceString = listOf(
        id,
        idUser,
        idAzienda,
        type.name,
        status.name,
        startDate.toString(),
        endDate.toString(),
        startTime?.toString() ?: "",
        endTime?.toString() ?: "",
        reason,
        submittedDate.toString(),
        approvedBy ?: "",
        approvedDate?.toString() ?: "",
        comments ?: "",
        totalDays?.toString() ?: "",
        totalHours?.toString() ?: ""
    ).joinToString("|")

    return HashManager.generateHash(absenceString)
}

// HASH PER LISTE - SEMPRE TRAMITE DOMAIN
fun List<AbsenceDto>.generateFirebaseHash(): String {
    // DTO → Domain → Hash
    val domainAbsences = this.map { it.toDomain() }
    return generateDomainAbsencesHash(domainAbsences)
}

fun List<AbsenceEntity>.generateCacheHash(): String {
    // Entity → Domain → Hash
    val domainAbsences = this.map { it.toDomain() }
    return generateDomainAbsencesHash(domainAbsences)
}

fun List<Absence>.generateDomainHash(): String {
    // Già Domain → Hash diretto
    return generateDomainAbsencesHash(this)
}

// FUNZIONE CENTRALE PER HASH LISTE DOMAIN
private fun generateDomainAbsencesHash(absences: List<Absence>): String {
    return HashManager.generateHashFromList(absences) { absence ->
        absence.generateAbsenceHash()
    }
}