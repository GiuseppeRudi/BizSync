package com.bizsync.ui.model

data class UserUi(
    val uid: String = "",
    val email: String = "",
    val nome: String = "Ciccio",
    val cognome : String = "Pasticcio",
    val photourl: String = "",
    val idAzienda: String = "",
    val isManager: Boolean = false,
    val ruolo: String = ""
)