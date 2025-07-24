package com.bizsync.backend.mapper


import com.bizsync.backend.dto.WeeklyShiftDto
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.utils.DateUtils.toFirebaseTimestamp
import com.bizsync.domain.utils.DateUtils.toLocalDate
import com.bizsync.domain.utils.DateUtils.toLocalDateTime
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

// Domain → DTO
fun WeeklyShift.toDto(): WeeklyShiftDto {
    return WeeklyShiftDto(
        idAzienda = idAzienda,
        weekStart = weekStart.toString(), // ISO
        createdBy = createdBy,
        createdAt = createdAt.toFirebaseTimestamp(),
        status = status.name,
        // Campi opzionali
        weekEnd = weekStart.plusDays(6).toString(), // calcolabile
        updatedAt = null,
        publishedAt = null,
        finalizedAt = null,
        notes = "",
        dipartimentiAttivi = dipartimentiAttivi.toDtoList()

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
        dipartimentiAttivi = dipartimentiAttivi.toDomainList()
    )
}

fun List<WeeklyShiftDto>.toDomainList(): List<WeeklyShift> = this.map {
    it.toDomain(it.idAzienda + "_" + it.weekStart) // oppure passa ID documento esternamente
}

fun List<WeeklyShift>.toDtoList(): List<WeeklyShiftDto> = this.map { it.toDto() }
