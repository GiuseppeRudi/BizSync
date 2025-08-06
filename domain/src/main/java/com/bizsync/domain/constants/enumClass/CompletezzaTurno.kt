package com.bizsync.domain.constants.enumClass

enum class CompletenezzaTurno {
    COMPLETO,      // Entrata + Uscita
    PARZIALE,      // Solo entrata O solo uscita
    ASSENTE,       // Nessuna timbratura
    NON_RICHIESTO  // Turno futuro
}
