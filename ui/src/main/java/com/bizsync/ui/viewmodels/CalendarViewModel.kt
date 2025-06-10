package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {



    private val _selectionData = MutableStateFlow<LocalDate?>(null)
    val selectionData : StateFlow<LocalDate?> = _selectionData

    private val _showDialogShift = MutableStateFlow(false)
    val showDialogShift : StateFlow<Boolean> = _showDialogShift

    fun onSelectionDataChanged(newValue : LocalDate)
    {
        _selectionData.value = newValue
    }

    fun onShowDialogShiftChanged(newValue : Boolean)
    {
        _showDialogShift.value = newValue
    }
}