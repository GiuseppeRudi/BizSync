package com.bizsync.ui.model

import com.bizsync.domain.model.User

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