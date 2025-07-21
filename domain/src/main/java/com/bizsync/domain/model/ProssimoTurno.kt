package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.TipoTimbratura
import java.time.Duration
import java.time.LocalDateTime

data class ProssimoTurno(
    val turno: Turno?,
    val tempoMancante: Duration?,
    val abilitaTimbratura: Boolean = false,
    val messaggioStato: String = "",
    val tipoTimbraturaNecessaria: TipoTimbratura = TipoTimbratura.ENTRATA,
    val haTimbratoEntrata: Boolean = false,
    val haTimbratoUscita: Boolean = false,
    val orarioPrevisto: LocalDateTime? = null
) {
    fun getTempoMancanteFormattato(): String {
        if (tempoMancante == null || turno == null) return "Nessun turno programmato"

        val totalMinutes = tempoMancante.toMinutes()
        val ore = tempoMancante.toHours()
        val minuti = totalMinutes % 60

        return when {
            totalMinutes < 0 && tipoTimbraturaNecessaria == TipoTimbratura.ENTRATA -> "In ritardo di ${-totalMinutes} min"
            totalMinutes < 0 && tipoTimbraturaNecessaria == TipoTimbratura.USCITA -> "Turno in corso"
            ore > 24 -> "${ore / 24} giorni"
            ore > 0 -> "$ore ore e $minuti minuti"
            totalMinutes == 0L -> "Ora!"
            else -> "$minuti minuti"
        }
    }

    fun isTimbraturaPossibile(): Boolean {
        if (turno == null || tempoMancante == null) return false

        val minutiMancanti = tempoMancante.toMinutes()

        return when (tipoTimbraturaNecessaria) {
            TipoTimbratura.ENTRATA -> {
                // Può timbrare l'entrata da 30 minuti prima a 30 minuti dopo l'inizio
                minutiMancanti >= -30 && minutiMancanti <= 30
            }
            TipoTimbratura.USCITA -> {
                // Può timbrare l'uscita da 30 minuti prima della fine a 2 ore dopo
                minutiMancanti >= -30 && minutiMancanti <= 120
            }
        }
    }

    fun getTestoPulsante(): String {
        return when (tipoTimbraturaNecessaria) {
            TipoTimbratura.ENTRATA -> "TIMBRA ENTRATA"
            TipoTimbratura.USCITA -> "TIMBRA USCITA"
        }
    }

    fun getStatoTurno(): String {
        if (turno == null) return "Nessun turno"

        return when {
            haTimbratoEntrata && haTimbratoUscita -> "Turno completato"
            haTimbratoEntrata && !haTimbratoUscita -> "Turno in corso"
            !haTimbratoEntrata -> "Turno programmato"
            else -> "Stato indefinito"
        }
    }
}