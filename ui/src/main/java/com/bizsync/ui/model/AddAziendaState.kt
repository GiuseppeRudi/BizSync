package com.bizsync.ui.model

// Stato specifico per questo ViewModel
data class AddAziendaState(
    val currentStep: Int = 1,
    val azienda: AziendaUi = AziendaUi(),
    val customSector: String = "",
    val resultMsg: String? = null,
    val isAgencyAdded: Boolean = false
)

