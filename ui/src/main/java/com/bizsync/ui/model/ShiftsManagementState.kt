package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.ShiftTimeFilter

data class ShiftsManagementState(
    val selectedFilter: ShiftTimeFilter = ShiftTimeFilter.DEFAULT_WINDOW,
    val turniWithTimbrature: List<TurnoWithTimbratureDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMoreData: Boolean = true
)
