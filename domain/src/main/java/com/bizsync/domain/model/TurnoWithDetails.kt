package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.StatoTurno
import java.time.LocalTime

data class TurnoWithDetails(
    val turno: Turno,
    val haTimbratoEntrata: Boolean = false,
    val haTimbratoUscita: Boolean = false,
    val orarioEntrataEffettivo: LocalTime? = null,
    val orarioUscitaEffettivo: LocalTime? = null,
    val minutiRitardo: Int = 0,
    val minutiAnticipo: Int = 0,
    val statoTurno: StatoTurno = StatoTurno.NON_INIZIATO
)
