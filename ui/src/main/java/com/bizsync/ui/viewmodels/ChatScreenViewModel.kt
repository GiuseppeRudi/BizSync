package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Shift(
    val id: Int,
    val title: String,
    val time: String
)


class ChatScreenViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shifts = MutableStateFlow<List<Shift>>(emptyList())
    val shifts: StateFlow<List<Shift>> = _shifts

    init {
        // Simula caricamento dati dopo 3 secondi
        viewModelScope.launch {
            delay(3000)
            _shifts.value = listOf(
                Shift(1, "Turno Mattina", "08:00 - 14:00"),
                Shift(2, "Turno Pomeriggio", "14:00 - 20:00"),
                Shift(3, "Turno Notte", "20:00 - 02:00")
            )
            _isLoading.value = false
        }
    }
}
