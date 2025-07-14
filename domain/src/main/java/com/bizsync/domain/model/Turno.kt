package com.bizsync.domain.model


import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

// @Serializable
data class Turno(
    val id: String = "",
    val nome: String = "",
    val idAzienda : String = "",
    val idDipendenti : List<String> = emptyList(),
    val orarioInizio: String = "", // Formato "HH:mm"
    val orarioFine: String = "",   // Formato "HH:mm"
    val dipendente: String = "",   // Nome o ID dipendente assegnato
    val dipartimentoId: String = "",
    val data: LocalDate = LocalDate.now(),
    val note: String = "",
    val isConfermato: Boolean = false,
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
) {
    constructor() : this("", "", "", emptyList(), "", "", "", "", LocalDate.now())

    /**
     * Calcola la durata del turno in ore
     */
    fun calcolaDurata(): Int {
        return try {
            val inizio = java.time.LocalTime.parse(orarioInizio)
            val fine = java.time.LocalTime.parse(orarioFine)
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
            val inizio1 = java.time.LocalTime.parse(this.orarioInizio)
            val fine1 = java.time.LocalTime.parse(this.orarioFine)
            val inizio2 = java.time.LocalTime.parse(altro.orarioInizio)
            val fine2 = java.time.LocalTime.parse(altro.orarioFine)

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