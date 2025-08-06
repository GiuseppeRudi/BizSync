package com.bizsync.backend.mapper

import com.bizsync.backend.dto.AbsenceDto
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Absence
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate
import com.bizsync.domain.utils.DateUtils.toLocalTime
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset


fun Absence.toDto(): AbsenceDto {
    return AbsenceDto(
        id = id,
        type = type.name,
        idUser = idUser,
        idAzienda = idAzienda,
        startDateTime = startDate.atTime(startTime ?: LocalTime.MIDNIGHT).toFirebaseTimestamp(),
        endDateTime = endDate.atTime(endTime ?: LocalTime.MIDNIGHT).toFirebaseTimestamp(),
        reason = reason,
        status = status.name,
        submittedDate = submittedDate.atStartOfDay().toFirebaseTimestamp(),
        approvedBy = approvedBy,
        approvedDate = approvedDate?.atStartOfDay()?.toFirebaseTimestamp(),
        comments = comments,
        submittedName = submittedName,
        totalHours = totalHours,
        totalDays = totalDays

    )
}

fun List<Absence>.toDtoList(): List<AbsenceDto> = this.map { it.toDto() }

fun List<AbsenceDto>.toDomainList(): List<Absence> = this.map { it.toDomain() }

fun AbsenceDto.toDomain(): Absence {
    return Absence(
        id = id,
        idUser = idUser,
        idAzienda = idAzienda,
        type = AbsenceType.valueOf(type),
        startDate = startDateTime!!.toLocalDate(),
        endDate = endDateTime!!.toLocalDate(),
        startTime = startDateTime.toLocalTime(),
        endTime = endDateTime.toLocalTime(),
        reason = reason,
        status = AbsenceStatus.valueOf(status),
        submittedDate = submittedDate!!.toLocalDate(),
        approvedBy = approvedBy,
        approvedDate = approvedDate?.toLocalDate(),
        comments = comments,
        submittedName = submittedName,
        totalHours = totalHours,
        totalDays = totalDays

    )
}

