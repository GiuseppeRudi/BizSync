package com.bizsync.model

import android.net.Uri
import com.google.firebase.firestore.Exclude

data class User(

    @get:Exclude
    var uid: String,
    var email : String,
    var nome : String,
    var cognome : String,
    var photourl : Uri?

){
    constructor() : this("","","","",null)
}
