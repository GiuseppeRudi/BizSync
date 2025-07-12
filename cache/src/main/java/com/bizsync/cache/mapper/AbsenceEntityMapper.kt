package com.bizsync.cache.mapper

import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Absence
import java.time.LocalDate
import java.time.LocalTime

fun AbsenceEntity.toDomain(): Absence {
    return Absence(
        id = this.id,
        idUser = this.idUser,
        submittedName = this.submittedName,
        idAzienda = this.idAzienda,
        type = AbsenceType.valueOf(this.type),
        startDate = LocalDate.parse(this.startDate),
        endDate = LocalDate.parse(this.endDate),
        startTime = this.startTime?.let { LocalTime.parse(it) },
        endTime = this.endTime?.let { LocalTime.parse(it) },
        reason = this.reason,
        status = AbsenceStatus.valueOf(this.status),
        submittedDate = LocalDate.parse(this.submittedDate),
        approvedBy = this.approver,
        approvedDate = this.approvedDate?.let { LocalDate.parse(it) },
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
        startDate = this.startDate.toString(),
        endDate = this.endDate.toString(),
        startTime = this.startTime?.toString(),
        endTime = this.endTime?.toString(),
        reason = this.reason,
        status = this.status.name,
        submittedDate = this.submittedDate.toString(),
        approver = this.approvedBy,
        approvedDate = this.approvedDate?.toString(),
        comments = this.comments,
        totalDays = this.totalDays,
        totalHours = this.totalHours,
        lastUpdated = System.currentTimeMillis()
    )
}



// List mappers
fun List<AbsenceEntity>.toDomainList(): List<Absence> = this.map { it.toDomain() }
fun List<Absence>.toEntityList(): List<AbsenceEntity> = this.map { it.toEntity() }
