package com.bizsync.ui.model


import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente

data class OnBoardingPianificaState(
    val currentStep: Int = 0,
    val aree: List<AreaLavoro> = emptyList(),
    val turni: List<TurnoFrequente> = emptyList(),
    val areePronte: Boolean = false,
    val turniPronti: Boolean = false,
    val nuovoTurno: TurnoFrequente = TurnoFrequente(),
    val nuovaArea: AreaLavoro = AreaLavoro(),
    val onDone: Boolean = false,
    val errorMsg: String? = null
)
