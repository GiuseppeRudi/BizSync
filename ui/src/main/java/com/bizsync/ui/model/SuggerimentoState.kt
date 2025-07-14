package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.viewmodels.DipartimentoStatus
import com.bizsync.ui.viewmodels.SuggerimentoTurno
import java.time.LocalDate



data class SuggerimentoState(


    val suggerimenti: List<SuggerimentoTurno> = emptyList(),
    )
