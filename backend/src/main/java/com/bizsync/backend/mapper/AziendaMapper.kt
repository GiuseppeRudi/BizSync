
package com.bizsync.backend.mapper

import android.util.Log
import com.bizsync.backend.dto.AziendaDto
import com.bizsync.domain.model.Azienda
import java.time.DayOfWeek

// Domain → DTO (per salvare su Firebase)
fun Azienda.toDto(): AziendaDto {

    val dto = AziendaDto(
        id = this.idAzienda,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDtoList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        latitudine = this.latitudine,
        longitudine = this.longitudine,
        tolleranzaMetri = this.tolleranzaMetri
    )

    return dto
}

// DTO → Domain (per leggere da Firebase)
fun AziendaDto.toDomain(): Azienda {
    val domain = Azienda(
        idAzienda = this.id,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDomainList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        latitudine = this.latitudine,
        longitudine = this.longitudine,
        tolleranzaMetri = this.tolleranzaMetri
    )

    return domain
}

