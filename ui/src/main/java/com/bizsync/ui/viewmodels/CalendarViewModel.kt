package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class CalendarViewModel : ViewModel() {

    var selectionData = mutableStateOf<LocalDate?>(null)
    var showDialogShift = mutableStateOf(false)

}