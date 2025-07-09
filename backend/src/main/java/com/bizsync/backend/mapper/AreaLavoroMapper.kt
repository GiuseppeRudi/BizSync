package com.bizsync.backend.mapper
import com.bizsync.backend.dto.AreaLavoroDto
import com.bizsync.domain.model.AreaLavoro
import java.time.DayOfWeek

// Singolo: Domain → DTO
fun AreaLavoro.toDto(): AreaLavoroDto {
    return AreaLavoroDto(
        id = this.id,
        nomeArea = this.nomeArea,
        orariSettimanali = this.orariSettimanali.mapKeys { it.key.name }
            .mapValues { mapOf("first" to it.value.first, "second" to it.value.second) } // CORREGGI: "first"/"second" non "start"/"end"
    )
}

// Singolo: DTO → Domain
fun AreaLavoroDto.toDomain(): AreaLavoro {
    return AreaLavoro(
        id = this.id,
        nomeArea = this.nomeArea,
        orariSettimanali = this.orariSettimanali.mapKeys {
            try {
                DayOfWeek.valueOf(it.key)
            } catch (e: Exception) {
                DayOfWeek.MONDAY // Valore di fallback
            }
        }.mapValues {
            val orario = it.value
            Pair(
                orario["first"] ?: "08:00",
                orario["second"] ?: "18:00"
            )
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
