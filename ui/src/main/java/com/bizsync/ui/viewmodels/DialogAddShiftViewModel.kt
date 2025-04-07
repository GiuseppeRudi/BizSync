package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.model.Turno
import kotlinx.coroutines.launch
import javax.inject.Inject


class DialogAddShiftViewModel : ViewModel() {

    var text = mutableStateOf("")
    var itemsList = mutableStateListOf<Turno>()


    fun caricaturni(){
        viewModelScope.launch {
            itemsList.clear()
        }
    }


}