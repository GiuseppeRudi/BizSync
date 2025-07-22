package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.ReportFilter

data class ReportsUiState(
    val selectedFilter: ReportFilter = ReportFilter.ALL_TIME,
    val selectedDepartment: String = "Tutti",
    val selectedTab: Int = 0,
    val reportData: ReportData = ReportData(
        contratti = emptyList(),
        users = emptyList(),
        absences = emptyList(),
        turni = emptyList()
    ),
    val departments: List<String> = listOf("Tutti"),
    val isLoading: Boolean = false,
    val error: String? = null
)

