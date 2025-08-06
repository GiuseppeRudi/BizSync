package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.usecases.UpdateUserPersonalInfoUseCase
import com.bizsync.ui.model.EditableUserFields
import com.bizsync.ui.model.EmployeeSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class EmployeeSettingsViewModel @Inject constructor(
    private val updateUserPersonalInfoUseCase: UpdateUserPersonalInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeSettingsState())
    val uiState = _uiState.asStateFlow()

    fun initializeWithUser(user: User) {
        _uiState.update { state ->
            state.copy(
                originalUser = user,
                editableFields = EditableUserFields(
                    numeroTelefono = user.numeroTelefono,
                    indirizzo = user.indirizzo,
                    codiceFiscale = user.codiceFiscale,
                    dataNascita = user.dataNascita,
                    luogoNascita = user.luogoNascita
                ),
                hasUnsavedChanges = false
            )
        }
    }

    fun updateNumeroTelefono(value: String) {
        _uiState.update { state ->
            val newFields = state.editableFields.copy(numeroTelefono = value)
            state.copy(
                editableFields = newFields,
                hasUnsavedChanges = hasChanges(state.originalUser, newFields)
            )
        }
    }

    fun updateIndirizzo(value: String) {
        _uiState.update { state ->
            val newFields = state.editableFields.copy(indirizzo = value)
            state.copy(
                editableFields = newFields,
                hasUnsavedChanges = hasChanges(state.originalUser, newFields)
            )
        }
    }

    fun updateCodiceFiscale(value: String) {
        // Validazione formato codice fiscale
        val cleanValue = value.uppercase().take(16)
        _uiState.update { state ->
            val newFields = state.editableFields.copy(codiceFiscale = cleanValue)
            state.copy(
                editableFields = newFields,
                hasUnsavedChanges = hasChanges(state.originalUser, newFields)
            )
        }
    }

    fun updateDataNascita(value: String) {
        _uiState.update { state ->
            val newFields = state.editableFields.copy(dataNascita = value)
            state.copy(
                editableFields = newFields,
                hasUnsavedChanges = hasChanges(state.originalUser, newFields)
            )
        }
    }

    fun updateLuogoNascita(value: String) {
        _uiState.update { state ->
            val newFields = state.editableFields.copy(luogoNascita = value)
            state.copy(
                editableFields = newFields,
                hasUnsavedChanges = hasChanges(state.originalUser, newFields)
            )
        }
    }

    private fun hasChanges(originalUser: User, editableFields: EditableUserFields): Boolean {
        return originalUser.numeroTelefono != editableFields.numeroTelefono ||
                originalUser.indirizzo != editableFields.indirizzo ||
                originalUser.codiceFiscale != editableFields.codiceFiscale ||
                originalUser.dataNascita != editableFields.dataNascita ||
                originalUser.luogoNascita != editableFields.luogoNascita
    }

    fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.hasUnsavedChanges) return

        // ✅ OPZIONE 1: Validazione nel ViewModel (come prima)
        if (!validateFields()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, error = null) }

                val updatedUser = currentState.originalUser.copy(
                    numeroTelefono = currentState.editableFields.numeroTelefono.trim(),
                    indirizzo = currentState.editableFields.indirizzo.trim(),
                    codiceFiscale = currentState.editableFields.codiceFiscale.trim(),
                    dataNascita = currentState.editableFields.dataNascita.trim(),
                    luogoNascita = currentState.editableFields.luogoNascita.trim()
                )

                // ✅ Usa Use Case invece del repository diretto
                when (val result = updateUserPersonalInfoUseCase(updatedUser)) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                originalUser = result.data,
                                isSaving = false,
                                hasUnsavedChanges = false,
                                successMessage = "Profilo aggiornato con successo!",
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                isSaving = false,
                                error = result.message ?: "Errore nel salvataggio del profilo"
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update { state ->
                            state.copy(
                                isSaving = false,
                                error = "Errore imprevisto nel salvataggio"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        error = "Errore imprevisto: ${e.message}"
                    )
                }
            }
        }
    }


    fun resetChanges() {
        val currentState = _uiState.value
        _uiState.update { state ->
            state.copy(
                editableFields = EditableUserFields(
                    numeroTelefono = currentState.originalUser.numeroTelefono,
                    indirizzo = currentState.originalUser.indirizzo,
                    codiceFiscale = currentState.originalUser.codiceFiscale,
                    dataNascita = currentState.originalUser.dataNascita,
                    luogoNascita = currentState.originalUser.luogoNascita
                ),
                hasUnsavedChanges = false,
                error = null
            )
        }
    }

    fun showConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun hideConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun validateFields(): Boolean {
        val fields = _uiState.value.editableFields

        // Validazioni opzionali
        if (fields.codiceFiscale.isNotEmpty() && !isValidCodiceFiscale(fields.codiceFiscale)) {
            _uiState.update { it.copy(error = "Codice fiscale non valido") }
            return false
        }

        if (fields.numeroTelefono.isNotEmpty() && !isValidPhoneNumber(fields.numeroTelefono)) {
            _uiState.update { it.copy(error = "Numero di telefono non valido") }
            return false
        }

        return true
    }

    private fun isValidCodiceFiscale(cf: String): Boolean {
        // Validazione semplice per codice fiscale italiano (16 caratteri alfanumerici)
        return cf.matches(Regex("^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$"))
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Validazione semplice per numero di telefono (solo cifre, +, spazi, -, parentesi)
        return phone.matches(Regex("^[+]?[0-9\\s\\-()]{6,20}$"))
    }
}