package com.bizsync.model

import com.google.firebase.firestore.Exclude

data class User(

    @get:Exclude
    var uid: String = "",

    var email : String

){
    constructor() : this("","")
}
