package com.bizsync.ui.model


data class UserUi(
    val uid: String = "",
    val email: String = "",
    val nome: String = "",
    val cognome: String = "",
    val photourl: String = "",
    val idAzienda: String = "",
    val isManager: Boolean = false,

    // CAMPI GESTITI DAL MANAGER (non modificabili dall'utente)
    val posizioneLavorativa: String = "",
    val dipartimento: String = "",

    //  CAMPI PERSONALI (inseriti dall'utente durante registrazione)
    val numeroTelefono: String = "",
    val indirizzo: String = "",
    val codiceFiscale: String = "",
    val dataNascita: String = "",
    val luogoNascita: String = ""
)