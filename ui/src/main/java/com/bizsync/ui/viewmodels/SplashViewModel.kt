package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


class SplashViewModel : ViewModel() {

    var isSplashVisible = mutableStateOf(true)

    var isLoading = mutableStateOf(true)
    var chooseAzienda = mutableStateOf(false)
    var creaAzienda = mutableStateOf(false)
    var chooseInvito = mutableStateOf(false)
    var terminate = mutableStateOf(false)

    fun hideSplash(){
        isSplashVisible.value = false
    }


}