package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.constants.enumClass.EmployeeSection

import com.bizsync.ui.model.EmployeeManagementState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeManagementViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeManagementState())
    val uiState: StateFlow<EmployeeManagementState> = _uiState

    fun setCurrentSection(section: EmployeeSection) {
        _uiState.update { it.copy(currentSection = section) }
    }

    fun loadEmployees() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

//                val result = repository.getEmployees()
//                _uiState.update { it.copy(employees = result) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nel caricamento dei dipendenti: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading employees", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeeContract(employeeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

//                val contract = repository.getEmployeeContract(employeeId)
//                _uiState.update { it.copy(contract = contract) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nel caricamento del contratto: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading contract", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeePastShifts(employeeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

//                val shifts = repository.getEmployeePastShifts(employeeId)
//                _uiState.update { it.copy(shifts = shifts) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nei turni passati: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading past shifts", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeeFutureShifts(employeeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

//                val shifts = repository.getEmployeeFutureShifts(employeeId)
//                _uiState.update { it.copy(shifts = shifts) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nei turni futuri: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading future shifts", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fireEmployee(employee: UserUi) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

//                repository.removeEmployee(employee.id)
//                _uiState.update {
//                    it.copy(
//                        employees = it.employees.filterNot { e -> e.id == employee.id },
//                        errorMessage = "Dipendente ${employee.name} ${employee.surname} rimosso con successo"
//                    )
//                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nella rimozione: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error firing employee", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearContract() {
        _uiState.update { it.copy(contract = null) }
    }

    fun clearShifts() {
        _uiState.update { it.copy(shifts = emptyList()) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSelectedDepartment(department: String) {
        _uiState.update { it.copy(selectedDepartment = department) }
    }

    fun updateSelectedEmployee(employee: UserUi?) {
        _uiState.update { it.copy(selectedEmployee = employee) }
    }
}
