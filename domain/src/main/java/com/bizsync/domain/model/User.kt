package com.bizsync.domain.model



data class User(
    val uid: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val photourl: String = "",
    val idAzienda: String = "",
    val manager: Boolean = false,
    val posizioneLavorativa: String = "",
    val dipartimento : String = ""
)

