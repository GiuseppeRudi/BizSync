package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Invito
import com.bizsync.model.domain.User
import com.bizsync.model.sealedClass.RuoliAzienda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class ScaffoldViewModel @Inject constructor() : ViewModel() {

    private val _fullScreen = MutableStateFlow<Boolean>(true)
    val fullScreen : StateFlow<Boolean> = _fullScreen


    fun onFullScreenChanged(newValue : Boolean)
    {
        _fullScreen.value = newValue
    }


}