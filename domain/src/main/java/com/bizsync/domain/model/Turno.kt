package com.bizsync.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Turno(
    @get:Exclude
    var idDocumento: String = "",

    val nome: String = "",
    val giorno: Timestamp = Timestamp.now()
    // Aggiungi altri campi della tua classe Turno qui
) {
    // Costruttore senza argomenti necessario per Firestore
    constructor() : this("", "", Timestamp.now())
}