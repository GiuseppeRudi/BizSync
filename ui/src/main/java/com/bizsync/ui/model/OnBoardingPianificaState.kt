package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek
import java.time.LocalTime

data class OnBoardingPianificaState(
    val currentStep: Int = 0,

    // Aree di lavoro
    val aree: List<AreaLavoro> = emptyList(),
    val nuovaArea: AreaLavoro = AreaLavoro(),
    val areePronte: Boolean = false,

    // Orari settimanali - PROPRIETÀ AGGIORNATE
    val selectedAree: List<String> = emptyList(), // ID delle aree selezionate per modifica
    val orariTemp: Map<DayOfWeek, Pair<LocalTime, LocalTime>> = emptyMap(), // Orari temporanei per la modifica
    val areeConOrariConfigurati: Set<String> = emptySet(), // ID delle aree che hanno già orari configurati
    val areeOrariConfigurati: Set<String> = emptySet(), // Nuova proprietà nello state

    // Turni frequenti
    val turni: List<TurnoFrequente> = emptyList(),
    val nuovoTurno: TurnoFrequente = TurnoFrequente(),
    val turniPronti: Boolean = false,

    // Stato generale
    val errorMsg: String? = null,
    val onDone: Boolean = false
)