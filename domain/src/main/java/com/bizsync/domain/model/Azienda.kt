package com.bizsync.domain.model

import com.google.firebase.firestore.Exclude

data class Azienda(

    @get:Exclude
    var idAzienda: String = "",
    var nome : String = "",
    var areeLavoro : List<AreaLavoro> = emptyList(),
    var turniFrequenti : List<TurnoFrequente> = emptyList()
) {
    // Costruttore senza argomenti necessario per Firestore
    constructor() : this("","",emptyList(),emptyList())
}