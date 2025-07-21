package com.bizsync.domain.model



data class User(
    val uid: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val photourl: String = "",
    val idAzienda: String = "",
    val isManager: Boolean = false,

    // CAMPI GESTITI DAL MANAGER
    val posizioneLavorativa: String = "",
    val dipartimento: String = "",

    // NUOVI CAMPI PERSONALI
    val numeroTelefono: String = "",
    val indirizzo: String = "",
    val codiceFiscale: String = "",
    val dataNascita: String = "",
    val luogoNascita: String = ""
)