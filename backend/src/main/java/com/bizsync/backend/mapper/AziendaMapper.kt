
package com.bizsync.backend.mapper

import android.util.Log
import com.bizsync.backend.dto.AziendaDto
import com.bizsync.domain.model.Azienda
import java.time.DayOfWeek

// Domain → DTO (per salvare su Firebase)
fun Azienda.toDto(): AziendaDto {
    Log.d("MAPPER_DEBUG", "=== MAPPATURA Domain → DTO ===")
    Log.d("MAPPER_DEBUG", "Azienda Domain:")
    Log.d("MAPPER_DEBUG", "  id: $idAzienda")
    Log.d("MAPPER_DEBUG", "  nome: $nome")
    Log.d("MAPPER_DEBUG", "  latitudine: $latitudine")
    Log.d("MAPPER_DEBUG", "  longitudine: $longitudine")
    Log.d("MAPPER_DEBUG", "  tolleranza: $tolleranzaMetri")

    val dto = AziendaDto(
        id = this.idAzienda,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDtoList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        giornoPubblicazioneTurni = this.giornoPubblicazioneTurni.name,
        // MAPPING COORDINATE
        latitudine = this.latitudine,
        longitudine = this.longitudine,
        tolleranzaMetri = this.tolleranzaMetri
    )

    Log.d("MAPPER_DEBUG", "AziendaDto creato:")
    Log.d("MAPPER_DEBUG", "  id: ${dto.id}")
    Log.d("MAPPER_DEBUG", "  nome: ${dto.nome}")
    Log.d("MAPPER_DEBUG", "  latitudine: ${dto.latitudine}")
    Log.d("MAPPER_DEBUG", "  longitudine: ${dto.longitudine}")
    Log.d("MAPPER_DEBUG", "  tolleranza: ${dto.tolleranzaMetri}")

    return dto
}

// DTO → Domain (per leggere da Firebase)
fun AziendaDto.toDomain(): Azienda {
    Log.d("MAPPER_DEBUG", "=== MAPPATURA DTO → Domain ===")
    Log.d("MAPPER_DEBUG", "AziendaDto ricevuto:")
    Log.d("MAPPER_DEBUG", "  id: $id")
    Log.d("MAPPER_DEBUG", "  nome: $nome")
    Log.d("MAPPER_DEBUG", "  latitudine: $latitudine")
    Log.d("MAPPER_DEBUG", "  longitudine: $longitudine")
    Log.d("MAPPER_DEBUG", "  tolleranza: $tolleranzaMetri")

    val domain = Azienda(
        idAzienda = this.id,
        nome = this.nome,
        areeLavoro = this.areeLavoro.toDomainList(),
        turniFrequenti = this.turniFrequenti,
        numDipendentiRange = this.numDipendentiRange,
        sector = this.sector,
        giornoPubblicazioneTurni = try {
            DayOfWeek.valueOf(this.giornoPubblicazioneTurni)
        } catch (e: Exception) {
            Log.w("MAPPER_DEBUG", "Errore parsing giorno pubblicazione: ${this.giornoPubblicazioneTurni}")
            DayOfWeek.FRIDAY // Valore di fallback
        },
        // MAPPING COORDINATE
        latitudine = this.latitudine,
        longitudine = this.longitudine,
        tolleranzaMetri = this.tolleranzaMetri
    )

    Log.d("MAPPER_DEBUG", "Azienda Domain creata:")
    Log.d("MAPPER_DEBUG", "  id: ${domain.idAzienda}")
    Log.d("MAPPER_DEBUG", "  nome: ${domain.nome}")
    Log.d("MAPPER_DEBUG", "  latitudine: ${domain.latitudine}")
    Log.d("MAPPER_DEBUG", "  longitudine: ${domain.longitudine}")
    Log.d("MAPPER_DEBUG", "  tolleranza: ${domain.tolleranzaMetri}")

    return domain
}

