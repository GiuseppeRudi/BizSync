package com.bizsync.domain.constants.enumClass

enum class CleanupStep(val message: String) {
    STARTING("Inizializzazione..."),
    CLEARING_CACHE("Pulizia cache locale..."),
    CLEARING_PREFERENCES("Rimozione preferenze..."),
    COMPLETED("Pulizia completata!"),
    ERROR("Errore durante la pulizia")
}