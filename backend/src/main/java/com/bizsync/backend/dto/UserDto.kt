package com.bizsync.backend.dto


data class UserDto(
    val uid: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val photourl: String,
    val idAzienda: String,
    val manager: Boolean,

    // CAMPI GESTITI DAL MANAGER
    val posizioneLavorativa: String,
    val dipartimento: String,

    // NUOVI CAMPI PERSONALI
    val numeroTelefono: String,
    val indirizzo: String,
    val codiceFiscale: String,
    val dataNascita: String,
    val luogoNascita: String
) {
    // Costruttore vuoto richiesto da Firebase
    constructor() : this(
        uid = "",
        email = "",
        nome = "",
        cognome = "",
        photourl = "",
        idAzienda = "",
        manager = false,
        posizioneLavorativa = "",
        dipartimento = "",
        numeroTelefono = "",
        indirizzo = "",
        codiceFiscale = "",
        dataNascita = "",
        luogoNascita = ""
    )
}




