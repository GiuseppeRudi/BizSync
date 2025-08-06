package com.bizsync.domain.constants.enumClass

enum class StatoTurnoDettagliato {
    NON_INIZIATO,           // Non ancora iniziato
    COMPLETATO_REGOLARE,    // Entrata e uscita in orario
    COMPLETATO_RITARDO,     // Completato ma con ritardi
    COMPLETATO_ANTICIPO,    // Completato con uscita anticipata
    PARZIALE_SOLO_ENTRATA, // Solo entrata, nessuna uscita
    ASSENTE,               // Nessuna timbratura
    TURNO_FUTURO           // Turno programmato futuro
}