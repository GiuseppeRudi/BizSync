package com.bizsync.cache.mapper

import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Absence

// AbsenceEntity → Absence (Domain)
fun AbsenceEntity.toDomain(): Absence {
    return Absence(
        id = this.id,
        idUser = this.idUser,
        submittedName = this.submittedName,
        idAzienda = this.idAzienda,
        type = AbsenceType.valueOf(this.type),
        startDate = this.startDate,              // ← Diretto, nessun parsing!
        endDate = this.endDate,                  // ← Diretto, nessun parsing!
        startTime = this.startTime,              // ← Diretto, nessun parsing!
        endTime = this.endTime,                  // ← Diretto, nessun parsing!
        reason = this.reason,
        status = AbsenceStatus.valueOf(this.status),
        submittedDate = this.submittedDate,      // ← Diretto, nessun parsing!
        approvedBy = this.approver,
        approvedDate = this.approvedDate,        // ← Diretto, nessun parsing!
        comments = this.comments,
        totalDays = this.totalDays,
        totalHours = this.totalHours
    )
}

// Absence (Domain) → AbsenceEntity
fun Absence.toEntity(): AbsenceEntity {
    return AbsenceEntity(
        id = this.id,
        idUser = this.idUser,
        submittedName = this.submittedName,
        idAzienda = this.idAzienda,
        type = this.type.name,
        startDate = this.startDate,              // ← Diretto, nessuna conversione!
        endDate = this.endDate,                  // ← Diretto, nessuna conversione!
        startTime = this.startTime,              // ← Diretto, nessuna conversione!
        endTime = this.endTime,                  // ← Diretto, nessuna conversione!
        reason = this.reason,
        status = this.status.name,
        submittedDate = this.submittedDate,      // ← Diretto, nessuna conversione!
        approver = this.approvedBy,
        approvedDate = this.approvedDate,        // ← Diretto, nessuna conversione!
        comments = this.comments,
        totalDays = this.totalDays,
        totalHours = this.totalHours,
        lastUpdated = System.currentTimeMillis()
    )
}

// List mappers
fun List<AbsenceEntity>.toDomainList(): List<Absence> = this.map { it.toDomain() }
fun List<Absence>.toEntityList(): List<AbsenceEntity> = this.map { it.toEntity() }