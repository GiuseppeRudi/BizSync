package com.bizsync.backend.dto

import com.google.firebase.firestore.Exclude


data class UserDto(

    @get:Exclude
    val uid: String = "",

    val email: String = "",
    val nome: String = "",
    val cognome: String = "",
    val photourl: String= "",
    val idAzienda: String= "",
    val manager: Boolean = false,

    // CAMPI GESTITI DAL MANAGER
    val posizioneLavorativa: String= "",
    val dipartimento: String= "",

    // CAMPI PERSONALI
    val numeroTelefono: String= "",
    val indirizzo: String= "",
    val codiceFiscale: String= "",
    val dataNascita: String= "",
    val luogoNascita: String= ""
)




