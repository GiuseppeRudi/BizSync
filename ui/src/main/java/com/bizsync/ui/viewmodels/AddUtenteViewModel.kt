package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddUtenteViewModel  @Inject constructor(private val userRepository: UserRepository): ViewModel() {

    private val _currentStep = MutableStateFlow<Int>(1)
    val currentStep : StateFlow<Int> = _currentStep

    private val _uid = MutableStateFlow("")
    val uid : StateFlow<String> = _uid

    private val _utente = MutableStateFlow<User?>(null)
    val utente : StateFlow<User?> = _utente

    private val _nome = MutableStateFlow("Ciccio")
    val nome: StateFlow<String> = _nome

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _cognome = MutableStateFlow("Pasticcio")
    val cognome: StateFlow<String> = _cognome

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl

    private val _isUserAdded = MutableStateFlow(false)
    val isUserAdded : StateFlow<Boolean> = _isUserAdded

    private val _erroreSalvataggio = MutableStateFlow<String?>(null)
    val erroreSalvataggio: StateFlow<String?> = _erroreSalvataggio


    fun setErrore(newValue: String?)
    {
        _erroreSalvataggio.value = newValue
    }


    fun addUserAndPropaga(userviewmodel: UserViewModel) {
        viewModelScope.launch {
            val utenteCreato = User("",email.value,nome.value,cognome.value,photoUrl.value,"")
            val success = userRepository.addUser(utenteCreato, uid.value)

            if(success)
            {
                _utente.value = utenteCreato
                userviewmodel.onUserChanged(utenteCreato)
                userviewmodel.onUidChanged(utenteCreato.uid)
                _isUserAdded.value = true
            }
            else
            {
                _erroreSalvataggio.value = "Errore durante il salvataggio dell'utente. Riprova."
            }

        }
    }



    fun onCurrentStepUp()
    {
        _currentStep.value = _currentStep.value + 1
    }

    fun onCurrentStepDown()
    {
        _currentStep.value = _currentStep.value - 1
    }

    fun onUidChanged(newValue : String)
    {
        _uid.value = newValue
    }

    fun onNomeChanged(newValue: String) {
        _nome.value = newValue
    }

    fun onEmailChanged(newValue: String) {
        _email.value = newValue
    }

    fun onCognomeChanged(newValue: String) {
        _cognome.value = newValue
    }

    fun onPhotoUrlChanged(newValue: String?) {
        _photoUrl.value = newValue
    }


}
