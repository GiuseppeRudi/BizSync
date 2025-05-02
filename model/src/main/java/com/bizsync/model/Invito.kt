package com.bizsync.model

import com.google.firebase.firestore.Exclude

data class Invito (
    @get:Exclude
    val id: String,


    val aziendaNome: String,
    val utente: String,
    val azienda: String,

    )
{
    constructor() : this("","","","")
}