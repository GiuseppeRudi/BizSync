package com.bizsync.backend.mapper


import com.bizsync.backend.dto.WeeklyShiftDto
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime

fun WeeklyShift.toDto(): WeeklyShiftDto {
    return WeeklyShiftDto(
        idAzienda = idAzienda,
        weekStart = weekStart.toString(), // ISO
        createdBy = createdBy,
        createdAt = createdAt.toFirebaseTimestamp(),
        status = status.name,
        weekEnd = weekStart.plusDays(6).toString(),
        updatedAt = null,
        publishedAt = null,
        finalizedAt = null,
        notes = "",
        dipartimentiAttivi = dipartimentiAttivi.toDtoList(),
        dipendentiAttivi = dipendentiAttivi.toDipendenteDtoList()

    )
}

// DTO → Domain
fun WeeklyShiftDto.toDomain(id: String): WeeklyShift {
    return WeeklyShift(
        id = id,
        idAzienda = idAzienda,
        weekStart = LocalDate.parse(weekStart),
        createdBy = createdBy,
        createdAt = createdAt?.toLocalDateTime() ?: LocalDateTime.MIN,
        status = WeeklyShiftStatus.valueOf(status),
        dipartimentiAttivi = dipartimentiAttivi.toDomainList(),
        dipendentiAttivi = dipendentiAttivi.toUserDomainList()
    )
}

fun List<WeeklyShiftDto>.toDomainList(): List<WeeklyShift> = this.map {
    it.toDomain(it.idAzienda + "_" + it.weekStart)
}

fun List<WeeklyShift>.toDtoList(): List<WeeklyShiftDto> = this.map { it.toDto() }
