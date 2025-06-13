package com.bizsync.ui.model


// Stato specifico per questo ViewModel
data class AddUtenteState(
    val currentStep: Int = 1,
    val uid: String = "",
    val userState: UserUi = UserUi(),
    val isUserAdded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)