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


fun Absence.toDto(): AbsenceDto {
    return AbsenceDto(
        id = id,
        type = type.name,
        startDateTime = startDate.atTime(startTime ?: LocalTime.MIDNIGHT).toTimestamp(),
        endDateTime = endDate.atTime(endTime ?: LocalTime.MIDNIGHT).toTimestamp(),
        reason = reason,
        status = status.name,
        submittedDate = submittedDate.atStartOfDay().toTimestamp(),
        approvedBy = approvedBy,
        approvedDate = approvedDate?.atStartOfDay()?.toTimestamp(),
        comments = comments
    )
}


fun AbsenceDto.toDomain(): Absence {
    return Absence(
        id = id,
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
        comments = comments
    )
}

fun Timestamp.toLocalDateTime(): LocalDateTime = toDate().toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()

fun LocalDateTime.toTimestamp(): Timestamp {
    val instant = this.atZone(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant.epochSecond, instant.nano)
}
fun Timestamp.toLocalDate(): LocalDate = toLocalDateTime().toLocalDate()
fun Timestamp.toLocalTime(): LocalTime = toLocalDateTime().toLocalTime()
