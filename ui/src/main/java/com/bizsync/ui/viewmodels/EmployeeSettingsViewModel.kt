package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmployeeSettingsState(
    val originalUser: User = User(),
    val editableFields: EditableUserFields = EditableUserFields(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showConfirmDialog: Boolean = false
)

data class EditableUserFields(
    val numeroTelefono: String = "",
    val indirizzo: String = "",
    val codiceFiscale: String = "",
    val dataNascita: String = "",
    val luogoNascita: String = ""
)

@HiltViewModel
class EmployeeSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val updatedUser = currentState.originalUser.copy(
                    numeroTelefono = currentState.editableFields.numeroTelefono.trim(),
                    indirizzo = currentState.editableFields.indirizzo.trim(),
                    codiceFiscale = currentState.editableFields.codiceFiscale.trim(),
                    dataNascita = currentState.editableFields.dataNascita.trim(),
                    luogoNascita = currentState.editableFields.luogoNascita.trim()
                )

                val result = userRepository.updateUserPersonalInfo(updatedUser)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                originalUser = updatedUser,
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
                        error = "Errore di connessione: ${e.message}"
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

suspend fun UserRepository.updateUserPersonalInfo(user: User): Resource<Unit> {
    return try {
        val success = this.updateUser(user, user.uid)
        if (success) {
            Resource.Success(Unit)
        } else {
            Resource.Error("Errore nell'aggiornamento del profilo")
        }
    } catch (e: Exception) {
        Resource.Error("Errore di connessione: ${e.message}")
    }
}