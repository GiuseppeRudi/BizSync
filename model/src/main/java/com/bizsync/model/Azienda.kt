package com.bizsync.model

import com.bizsync.model.Turno
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Azienda(

    @get:Exclude
    var idAzienda: String = "",
    var Nome : String
) {
    // Costruttore senza argomenti necessario per Firestore
    constructor() : this("","")
}

