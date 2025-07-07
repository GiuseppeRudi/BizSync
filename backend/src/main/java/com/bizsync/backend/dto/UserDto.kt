package com.bizsync.backend.dto


import com.google.firebase.firestore.Exclude

data class UserDto(


    @get:Exclude
    val uid: String = "",

    val email: String = "",
    val nome: String = "",
    val cognome: String = "",
    val photourl: String = "",
    val idAzienda: String = "",
    val manager: Boolean = false,
    val posizioneLavorativa: String = "",
    val dipartimento : String = ""

)



