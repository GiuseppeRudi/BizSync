package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.CompletenezzaTurno
import com.bizsync.domain.constants.enumClass.StatoTurnoDettagliato
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.Turno

data class TurnoWithTimbratureDetails(
    val turno: Turno,
    val timbratureEntrata: Timbratura? = null,
    val timbratureUscita: Timbratura? = null,
    val statoTurno: StatoTurnoDettagliato,
    val minutiRitardoEntrata: Int = 0,
    val minutiRitardoUscita: Int = 0,
    val minutiLavoratiEffettivi: Int = 0,
    val completezza: CompletenezzaTurno
)
