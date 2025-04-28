package com.bizsync.model

import android.net.Uri
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class User(

    @get:Exclude
    var uid: String,
    var email : String,
    var nome : String,
    var cognome : String,
    var photourl : String? = null,
    var idAzienda : String?
){
    constructor() : this("","","","","","")
}
