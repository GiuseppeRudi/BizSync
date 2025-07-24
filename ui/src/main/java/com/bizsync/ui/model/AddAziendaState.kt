package com.bizsync.ui.model

import android.location.Address

data class AddAziendaState(
    val azienda: AziendaUi = AziendaUi(),
    val currentStep: Int = 1,
    val isAgencyAdded: Boolean = false,
    val resultMsg: String? = null,

    val indirizzoInput: String = "",
    val indirizziCandidati: List<Address> = emptyList(),
    val indirizzoSelezionato: Address? = null,
    val isGeocoding: Boolean = false,
    val geocodingError: String? = null
)