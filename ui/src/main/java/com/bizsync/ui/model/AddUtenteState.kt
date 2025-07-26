package com.bizsync.ui.model


data class AddUtenteState(
    val currentStep: Int = 1,
    val uid: String = "",
    val user: UserUi = UserUi(),
    val isUserAdded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)