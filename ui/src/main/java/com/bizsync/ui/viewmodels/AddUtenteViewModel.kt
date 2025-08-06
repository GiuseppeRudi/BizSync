package com.bizsync.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.usecases.AddUserUseCase
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUi
import com.bizsync.ui.model.AddUtenteState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddUtenteViewModel @Inject constructor(
    private val addUserUseCase: AddUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUtenteState(user = UserUi()))
    val uiState: StateFlow<AddUtenteState> = _uiState

    fun addUser() {
        // ✅ OPZIONE 1: Validazione nel ViewModel (come prima)
        if (!validateUser()) return

        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                Log.d("ADD_USER_DEBUG", "=== INIZIO CREAZIONE UTENTE ===")
                Log.d("ADD_USER_DEBUG", "UID: ${_uiState.value.uid}")
                Log.d("ADD_USER_DEBUG", "Dati utente:")
                Log.d("ADD_USER_DEBUG", "  Nome: ${_uiState.value.user.nome}")
                Log.d("ADD_USER_DEBUG", "  Cognome: ${_uiState.value.user.cognome}")
                Log.d("ADD_USER_DEBUG", "  Email: ${_uiState.value.user.email}")

                val utenteCreato = _uiState.value.user.toDomain().copy(uid = "")

                // ✅ Usa Use Case invece del repository diretto
                when (val result = addUserUseCase(utenteCreato, _uiState.value.uid)) {
                    is Resource.Success -> {
                        Log.d("ADD_USER_DEBUG", "✅ Utente creato con successo")

                        updateState {
                            it.copy(
                                user = result.data.toUi(),
                                isLoading = false,
                                isUserAdded = true
                            )
                        }
                    }

                    is Resource.Error -> {
                        Log.e("ADD_USER_DEBUG", "❌ Errore durante il salvataggio: ${result.message}")
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Errore durante il salvataggio dell'utente. Riprova."
                            )
                        }
                    }

                    is Resource.Empty -> {
                        Log.e("ADD_USER_DEBUG", "⚠️ Risultato vuoto")
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = "Errore durante il salvataggio dell'utente. Riprova."
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("ADD_USER_DEBUG", "🚨 Eccezione imprevista: ${e.message}")
                updateState {
                    it.copy(
                        isLoading = false,
                        error = "Errore imprevisto: ${e.message}"
                    )
                }
            }
        }
    }


    fun onNomeChanged(newValue: String) {
        updateUserState { it.copy(nome = newValue) }
    }

    fun onCognomeChanged(newValue: String) {
        updateUserState { it.copy(cognome = newValue) }
    }

    fun onEmailChanged(newValue: String) {
        updateUserState { it.copy(email = newValue) }
    }

    fun onPhotoUrlChanged(newValue: String) {
        updateUserState { it.copy(photourl = newValue) }
    }

    fun onNumeroTelefonoChanged(newValue: String) {
        updateUserState { it.copy(numeroTelefono = newValue) }
    }

    fun onIndirizzoChanged(newValue: String) {
        updateUserState { it.copy(indirizzo = newValue) }
    }

    fun onCodiceFiscaleChanged(newValue: String) {
        val cleanValue = newValue.filter { it.isLetterOrDigit() }.take(16)
        updateUserState { it.copy(codiceFiscale = cleanValue) }
    }

    fun onDataNascitaChanged(newValue: String) {
        // Semplicemente mantieni solo i numeri, massimo 8 cifre
        val digits = newValue.filter { it.isDigit() }.take(8)
        updateUserState { it.copy(dataNascita = digits) }
    }

    fun onLuogoNascitaChanged(newValue: String) {
        updateUserState { it.copy(luogoNascita = newValue) }
    }

    fun onCurrentStepUp() {
        val canProceed = canProceedToNextStep(_uiState.value.currentStep)
        if (canProceed) {
            updateState { it.copy(currentStep = it.currentStep + 1) }
        }
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

    // FUNZIONE PER VERIFICARE SE SI PUÒ PROCEDERE AL PROSSIMO STEP
    fun canProceedToNextStep(currentStep: Int): Boolean {
        val userState = _uiState.value.user
        return when (currentStep) {
            1 -> {
                // Step 1: Nome e cognome obbligatori, email deve essere valida
                userState.nome.isNotBlank() &&
                        userState.cognome.isNotBlank() &&
                        userState.email.isNotBlank() &&
                        Patterns.EMAIL_ADDRESS.matcher(userState.email).matches()
            }
            2 -> {
                // Step 2: Tutti i campi sono opzionali, quindi sempre true
                true
            }
            3 -> {
                // Step 3: Tutti i campi sono opzionali, quindi sempre true
                // Ma se il codice fiscale è inserito, deve avere almeno 11 caratteri
                userState.codiceFiscale.isEmpty() || userState.codiceFiscale.length >= 11
            }
            4 -> {
                // Step 4: Riepilogo, sempre true se siamo arrivati qui
                true
            }
            else -> true
        }
    }

    private fun updateState(update: (AddUtenteState) -> AddUtenteState) {
        _uiState.value = update(_uiState.value)
    }

    private fun updateUserState(update: (UserUi) -> UserUi) {
        updateState { it.copy(user = update(it.user)) }
    }

    private fun validateUser(): Boolean {
        val userState = _uiState.value.user

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
            !Patterns.EMAIL_ADDRESS.matcher(userState.email).matches() -> {
                setErrore("Formato email non valido")
                false
            }
            // Validazioni opzionali per campi aggiuntivi
            userState.numeroTelefono.isNotBlank() && userState.numeroTelefono.length < 8 -> {
                setErrore("Numero di telefono non valido")
                false
            }
            userState.codiceFiscale.isNotBlank() && userState.codiceFiscale.length < 11 -> {
                setErrore("Codice fiscale non valido (minimo 11 caratteri)")
                false
            }
            else -> {
                setErrore(null)
                true
            }
        }
    }
}
