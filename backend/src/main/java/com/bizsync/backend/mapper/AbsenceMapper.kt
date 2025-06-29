package com.bizsync.backend.mapper

import com.bizsync.backend.dto.AbsenceDto
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Absence
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset


fun Absence.toDto(): AbsenceDto {
    return AbsenceDto(
        id = id,
        type = type.name,
        idUser = idUser,
        idAzienda = idAzienda,
        startDateTime = startDate.atTime(startTime ?: LocalTime.MIDNIGHT).toTimestamp(),
        endDateTime = endDate.atTime(endTime ?: LocalTime.MIDNIGHT).toTimestamp(),
        reason = reason,
        status = status.name,
        submittedDate = submittedDate.atStartOfDay().toTimestamp(),
        approvedBy = approvedBy,
        approvedDate = approvedDate?.atStartOfDay()?.toTimestamp(),
        comments = comments,
        submittedName = submittedName

    )
}


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
        submittedName = submittedName

    )
}

fun LocalDateTime.toTimestamp(): Timestamp {
    val instant = this.atZone(ZoneOffset.UTC).toInstant() // Usa UTC invece di systemDefault
    return Timestamp(instant.epochSecond, instant.nano)
}

fun Timestamp.toLocalDate(): LocalDate = this.toDate().toInstant()
    .atZone(ZoneOffset.UTC)
    .toLocalDate()

fun Timestamp.toLocalTime(): LocalTime = this.toDate().toInstant()
    .atZone(ZoneOffset.UTC)
    .toLocalTime()
