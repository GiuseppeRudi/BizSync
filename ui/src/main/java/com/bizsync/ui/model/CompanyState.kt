package com.bizsync.ui.model


import com.bizsync.domain.constants.enumClass.CompanyOperation
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.DialogStatusType
import java.time.DayOfWeek
import java.time.LocalTime

data class CompanyState(
    val selectedOperation : CompanyOperation? = null,
    val onBoardingDone: Boolean? = null,

    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val isLoading: Boolean = false,

    val  areeModificate : List<AreaLavoro> = emptyList(),

    val orariSettimanaliModificati: Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>> = emptyMap(),
    val editingOrariArea: String? = null, // ID dell'area di cui stiamo modificando gli orari

    val showOrariDialog: Boolean = false,

    val orariTemp: Map<DayOfWeek, Pair<LocalTime, LocalTime>> = emptyMap(), // Orari temporanei durante la modifica

    val showAddDialog : Boolean = false,
    val editingArea : AreaLavoro? = null,

    val hasChanges : Boolean = false,

    // NUOVE PROPRIETÀ PER GIORNO PUBBLICAZIONE
    val giornoPublicazioneTemp: DayOfWeek? = null, // Giorno temporaneo durante la modifica
    val showGiornoPublicazioneDialog: Boolean = false,
    val hasGiornoPublicazioneChanges: Boolean = false,
    val hasGiornoPubblicato : Boolean = false,
)
