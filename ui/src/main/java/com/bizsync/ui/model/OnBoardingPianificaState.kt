package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek
import java.time.LocalTime

data class OnBoardingPianificaState(
    val currentStep: Int = 0,  // Inizia da 0 (WelcomeStep)
    val aree: List<AreaLavoro> = emptyList(),
    val nuovaArea: AreaLavoro = AreaLavoro(),
    val turni: List<TurnoFrequente> = emptyList(),
    val nuovoTurno: TurnoFrequente = TurnoFrequente(),
    val areePronte: Boolean = false,
    val turniPronti: Boolean = false,
    val errorMsg: String? = null,
    val onDone: Boolean = false,

    // Campi per gestione orari
    val selectedAree: List<String> = emptyList(),
    val orariTemp: Map<DayOfWeek, Pair<LocalTime, LocalTime>> = emptyMap(),
    val areeOrariConfigurati: Set<String> = emptySet()
)