package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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