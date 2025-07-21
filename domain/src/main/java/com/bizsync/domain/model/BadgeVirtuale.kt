package com.bizsync.domain.model


data class BadgeVirtuale(
    val idDipendente: String,
    val nome: String,
    val cognome: String,
    val matricola: String,
    val posizioneLavorativa: String,
    val dipartimento: String,
    val fotoUrl: String,
    val idAzienda: String,
    val nomeAzienda: String,
    val creationTimestamp: Long = System.currentTimeMillis()
) {
    fun getFullName() = "$nome $cognome"

    fun generateQRData(): String {
        return "BADGE:$idDipendente:$matricola:$idAzienda:$creationTimestamp"
    }
}
