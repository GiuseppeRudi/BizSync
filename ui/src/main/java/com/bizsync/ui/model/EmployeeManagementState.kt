package com.bizsync.ui.model


import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno

data class EmployeeManagementState(
    var currentSection  : EmployeeSection = EmployeeSection.MAIN ,
    val selectedEmployee : UserUi? = null,
    val employees: List<UserUi> = emptyList(),
    val contract: Contratto? = null,
    val shifts: List<Turno> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery : String = "",
    val selectedDepartment : String = "Tutti",
)
