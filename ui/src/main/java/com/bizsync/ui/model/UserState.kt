package com.bizsync.ui.model



data class UserState(
    val user : UserUi = UserUi(),
    val azienda : AziendaUi = AziendaUi(),
    val hasLoadedUser: Boolean = false,
    val hasLoadedAgency: Boolean = false,
    val errorMsg : String? = null,
    val successMsg : String? = null,
    val checkUser : Boolean? = null
)