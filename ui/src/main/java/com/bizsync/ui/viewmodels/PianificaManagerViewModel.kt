package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import com.bizsync.ui.model.EmployeeState
import com.bizsync.ui.model.ManagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject



@HiltViewModel
class PianificaManagerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerState())
    val uiState: StateFlow<ManagerState> = _uiState


    fun getTurniSettimali(startWeek : LocalDate)
    {
        // orchestrator
    }



}