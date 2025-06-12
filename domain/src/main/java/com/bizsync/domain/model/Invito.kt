package com.bizsync.domain.model

import com.google.firebase.firestore.Exclude

data class Invito (
    @get:Exclude
    val id: String,


    val aziendaNome: String,
    val email: String,
    val azienda: String,
    val manager : Boolean,
    val nomeRuolo : String,
    val stato : String

    )
{
    constructor() : this("","","","",false,"","")
}