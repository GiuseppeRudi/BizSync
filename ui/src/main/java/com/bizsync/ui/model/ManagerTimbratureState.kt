package com.bizsync.ui.model

import com.bizsync.domain.model.Timbratura
import java.time.LocalDate

data class ManagerTimbratureState(
    val idAzienda: String = "",
    val timbrature: List<Timbratura> = emptyList(),
    val timbratureAnomale: List<Timbratura> = emptyList(),
    val timbratureDaVerificare: List<Timbratura> = emptyList(),
    val selectedTimbratura: Timbratura? = null,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = ""
)