package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUiState
import com.bizsync.ui.model.AddUtenteState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class AddUtenteViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Stato principale consolidato
    private val _uiState = MutableStateFlow(
        AddUtenteState(
            userState = UserUi()
        )
    )
    val uiState: StateFlow<AddUtenteState> = _uiState

    fun addUserAndPropaga(userviewmodel: UserViewModel) {
        if (!validateUser()) return

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            Log.d("CONTROLLO_USER_FINAL", _uiState.value.uid)

            try {
                val utenteCreato = _uiState.value.userState.toDomain().copy(uid = "")
                val success = userRepository.addUser(utenteCreato, _uiState.value.uid)

                if (success) {
                    val utenteConUid = utenteCreato.copy(uid = _uiState.value.uid)

                    updateState {
                        it.copy(
                            userState = utenteConUid.toUiState(),
                            isLoading = false,
                            isUserAdded = true
                        )
                    }

                }
                else {
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = "Errore durante il salvataggio dell'utente. Riprova."
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Errore sconosciuto"
                    )
                }
            }
        }
    }

    // Metodi per aggiornare i campi utente
    fun onNomeChanged(newValue: String) {
        updateUserState { it.copy(nome = newValue) }
    }

    // Metodi per aggiornare i campi utente
    fun onCognomeChanged(newValue: String) {
        updateUserState { it.copy(cognome = newValue) }
    }

    fun onEmailChanged(newValue: String) {
        updateUserState { it.copy(email = newValue) }
    }

    fun onPhotoUrlChanged(newValue: String) {
        updateUserState { it.copy(photourl = newValue) }
    }

    // Metodi per la navigazione
    fun onCurrentStepUp() {
        updateState { it.copy(currentStep = it.currentStep + 1) }
    }

    fun onCurrentStepDown() {
        updateState { it.copy(currentStep = it.currentStep - 1) }
    }

    fun onUidChanged(newValue: String) {
        updateState { it.copy(uid = newValue) }
    }

    fun setErrore(newValue: String?) {
        updateState { it.copy(error = newValue) }
    }

    // Utility methods
    private fun updateState(update: (AddUtenteState) -> AddUtenteState) {
        _uiState.value = update(_uiState.value)
    }

    private fun updateUserState(update: (UserUi) -> UserUi) {
        updateState { it.copy(userState = update(it.userState)) }
    }

    private fun validateUser(): Boolean {
        val userState = _uiState.value.userState

        return when {
            userState.nome.isBlank() -> {
                setErrore("Il nome è obbligatorio")
                false
            }

            userState.cognome.isBlank() -> {
                setErrore("Il cognome è obbligatorio")
                false
            }

            userState.email.isBlank() -> {
                setErrore("L'email è obbligatoria")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(userState.email).matches() -> {
                setErrore("Formato email non valido")
                false
            }
            else -> {
                setErrore(null)
                true
            }
        }
    }

    fun resetState() {
        _uiState.value = AddUtenteState()
    }
}

