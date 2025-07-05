package com.bizsync.backend.dto

import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.model.Ccnlnfo
import com.google.firebase.firestore.Exclude


data class InvitoDto (
    @get:Exclude
    val id: String,


    val aziendaNome: String,
    val email: String,
    val idAzienda: String,
    val manager : Boolean,
    val nomeRuolo : String,
    val stato : String,
    val ccnlInfo : Ccnlnfo

)
{
    constructor() : this("","","","",false,"", "INPENDING", Ccnlnfo())
}