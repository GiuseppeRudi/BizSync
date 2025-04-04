package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class DialogAddShiftViewModel : ViewModel() {

    var text = mutableStateOf("")
    var itemsList = mutableStateListOf<String>()
}