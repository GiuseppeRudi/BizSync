package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.traceEventEnd
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SplashViewModel : ViewModel() {

    private  val _isSplashVisible = MutableStateFlow(true)
    val isSplashVisible : StateFlow<Boolean> = _isSplashVisible

    fun onSplashVisibleChanged(newValue : Boolean)
    {
        _isSplashVisible.value = newValue
    }

    fun hideSplash(){
        _isSplashVisible.value = false
    }

}