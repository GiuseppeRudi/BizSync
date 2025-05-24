package com.bizsync.model.domain

import com.google.firebase.firestore.Exclude

data class User(

    @get:Exclude
    var uid: String,
    var email : String,
    var nome : String,
    var cognome : String,
    var photourl : String = "",
    var idAzienda : String = "",
    var manager : Boolean = false,
    var ruolo : String = ""
){
    constructor() : this("","","","","","",false,"")
}