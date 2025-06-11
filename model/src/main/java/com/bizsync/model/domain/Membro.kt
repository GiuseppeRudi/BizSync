package com.bizsync.model.domain

// Struttura dati semplificata
data class Membro(
    val id: String = "",
    val nome: String = "",
    val cognome: String = "",
    val ruolo: String = "", // Ruolo fisso del dipendente
    val email: String = "",
    val telefono: String = "",
    val isAttivo: Boolean = true
) {
    val nomeCompleto: String
        get() = "$nome $cognome"

    val iniziali: String
        get() = "${nome.firstOrNull()?.uppercaseChar() ?: ""}${cognome.firstOrNull()?.uppercaseChar() ?: ""}"
}

