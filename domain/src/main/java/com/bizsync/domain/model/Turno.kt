package com.bizsync.domain.model


import com.bizsync.domain.constants.enumClass.EsitoTurno
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

// @Serializable
data class Turno(
    val id: String = UUID.randomUUID().toString(),
    val titolo: String = "",
    val idAzienda : String = "",
    val idDipendenti : List<String> = emptyList(),
    val dipartimentoId: String = "",
    val idFirebase : String = "",

    val data: LocalDate = LocalDate.now(),
    val orarioInizio: LocalTime = LocalTime.now(),
    val orarioFine: LocalTime = LocalTime.now(),

    val zoneLavorative: Map<String, ZonaLavorativa> = emptyMap(),


    val note: List<Nota> = emptyList(),
    val pause : List<Pausa> = emptyList(),

    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
) {
    fun getZonaLavorativaDipendente(idDipendente: String): ZonaLavorativa {
        return zoneLavorative[idDipendente] ?: ZonaLavorativa.IN_SEDE
    }

    /**
     * Calcola la durata del turno in ore
     */
    fun calcolaDurata(): Int {
        return try {
            val inizio = orarioInizio
            val fine = orarioFine
            val durataMinuti = fine.toSecondOfDay() - inizio.toSecondOfDay()
            (durataMinuti / 3600).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Verifica se il turno si sovrappone con un altro
     */
    fun siSovrappongeCon(altro: Turno): Boolean {
        if (this.data != altro.data || this.dipartimentoId != altro.dipartimentoId) {
            return false
        }

        return try {
            val inizio1 = orarioInizio
            val fine1 = orarioFine
            val inizio2 = altro.orarioInizio
            val fine2 = altro.orarioFine

            !(fine1 <= inizio2 || fine2 <= inizio1)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica se il turno è adiacente a un altro
     */
    fun eAdiacenteA(altro: Turno): Boolean {
        if (this.data != altro.data || this.dipartimentoId != altro.dipartimentoId) {
            return false
        }

        return this.orarioFine == altro.orarioInizio || altro.orarioFine == this.orarioInizio
    }
}