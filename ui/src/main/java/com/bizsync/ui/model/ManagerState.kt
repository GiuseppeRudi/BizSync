package com.bizsync.ui.model


import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno
import com.bizsync.ui.components.DialogStatusType
import java.time.DayOfWeek
import java.time.LocalDate


data class ManagerState(
    val loading : Boolean = true,
    val turniSettimanali : Map<DayOfWeek,List<Turno>> = emptyMap(),
    val turniGiornalieri : Map<String , List<Turno>> = emptyMap(),
    val turniGiornalieriDip : List<Turno> = emptyList(),
//    val turniSettimanaliDip
)
