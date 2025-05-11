package com.bizsync.ui.viewmodels

import com.bizsync.backend.repository.InvitoRepository

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GestioneViewModel  @Inject constructor(private val invitoRepository: InvitoRepository): ViewModel() {


    private val _showInvite = MutableStateFlow(false)
    val showDialog : StateFlow<Boolean> = _showInvite


    fun onShowDialogChanged(newValue : Boolean)
    {
        _showInvite.value = newValue
    }


}
