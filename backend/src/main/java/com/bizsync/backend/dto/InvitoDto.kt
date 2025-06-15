package com.bizsync.backend.dto

import com.bizsync.domain.constants.StatusInvite
import com.google.firebase.firestore.Exclude


data class InvitoDto (
    @get:Exclude
    val id: String,


    val aziendaNome: String,
    val email: String,
    val idAzienda: String,
    val manager : Boolean,
    val nomeRuolo : String,
    val stato : String

)
{
    constructor() : this("","","","",false,"", "INPENDING")
}