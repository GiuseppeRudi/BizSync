package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject



@HiltViewModel
class ScaffoldViewModel @Inject constructor() : ViewModel() {

    private val _isFullScreen = MutableStateFlow<Boolean>(true)
    val isFullScreen : StateFlow<Boolean> = _isFullScreen

    fun onFullScreenChanged(newValue : Boolean)
    {
        _isFullScreen.value = newValue
    }

}