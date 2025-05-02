package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {


    var isSplashVisible = mutableStateOf(true)


    fun hideSplash(){
        isSplashVisible.value = false
    }


}