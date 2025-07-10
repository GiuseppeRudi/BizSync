package com.bizsync.domain.model

data class Contratto(
    val id: String = "", // generato da Firestore
    val idDipendente: String = "",
    val idAzienda: String = "",
    val emailDipendente: String = "",
    val posizioneLavorativa: String = "",
    val dipartimento: String = "",
    val tipoContratto: String = "",
    val oreSettimanali: String = "",
    val settoreAziendale: String = "",
    val dataInizio: String = "",
    val ccnlInfo: Ccnlnfo = Ccnlnfo(),

    // Nuove variabili di utilizzo
    val ferieUsate: Int = 0,             // in giorni
    val rolUsate: Int = 0,               // in ore
    val malattiaUsata: Int = 0           // in giorni
)
