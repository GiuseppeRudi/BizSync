package com.bizsync.backend.mapper
import com.bizsync.backend.dto.AreaLavoroDto
import com.bizsync.domain.model.AreaLavoro
import java.time.DayOfWeek

import java.time.LocalTime

fun AreaLavoro.toDto(): AreaLavoroDto {
    return AreaLavoroDto(
        nomeArea = this.nomeArea,
        orariSettimanali = this.orariSettimanali.mapKeys { it.key.name }
            .mapValues { entry ->
                val (start, end) = entry.value
                mapOf(
                    "start" to start.toString(),  // LocalTime to String "HH:mm:ss" o "HH:mm"
                    "end" to end.toString()
                )
            }
    )
}

// DTO → Domain
fun AreaLavoroDto.toDomain(): AreaLavoro {
    return AreaLavoro(
        nomeArea = this.nomeArea,
        orariSettimanali = this.orariSettimanali.mapKeys {
            try {
                DayOfWeek.valueOf(it.key)
            } catch (e: Exception) {
                DayOfWeek.MONDAY // fallback
            }
        }.mapValues { entry ->
            val orariMap = entry.value
            val startStr = orariMap["start"] ?: "08:00"
            val endStr = orariMap["end"] ?: "18:00"
            Pair(LocalTime.parse(startStr), LocalTime.parse(endStr))
        }
    )
}


// Lista: Domain → DTO
fun List<AreaLavoro>.toDtoList(): List<AreaLavoroDto> {
    return this.map { it.toDto() }
}

// Lista: DTO → Domain
fun List<AreaLavoroDto>.toDomainList(): List<AreaLavoro> {
    return this.map { it.toDomain() }
}
