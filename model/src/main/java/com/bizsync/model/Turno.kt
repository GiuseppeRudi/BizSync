package com.bizsync.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Turno(
    @get:Exclude
    var idDocumento: String = "",

    val nome: String = "",
    val data: Timestamp = Timestamp.now()
    // Aggiungi altri campi della tua classe Turno qui
) {
    // Costruttore senza argomenti necessario per Firestore
    constructor() : this("", "", Timestamp.now())
}



// val docRef = db.collection("turni").add(nuovoTurno).await()
// val idGenerato = docRef.id
