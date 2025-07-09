package com.bizsync.backend.mapper


import com.bizsync.backend.dto.AziendaDto
import com.bizsync.domain.model.Azienda
import java.time.DayOfWeek




fun Azienda.toDto(): AziendaDto {
    return AziendaDto(
        id = this.idAzienda,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDtoList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        giornoPubblicazioneTurni = this.giornoPubblicazioneTurni.name
    )
}

// Singolo: DTO → Domain
fun AziendaDto.toDomain(): Azienda {
    return Azienda(
        idAzienda = this.id,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDomainList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        giornoPubblicazioneTurni = try {
            DayOfWeek.valueOf(this.giornoPubblicazioneTurni)
        } catch (e: Exception) {
            DayOfWeek.FRIDAY // Valore di fallback
        }
    )
}





