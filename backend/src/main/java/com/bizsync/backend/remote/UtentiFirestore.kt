package com.bizsync.backend.remote



object UtentiFirestore {
    const val COLLECTION = "utenti"

    object Fields {
        const val UID                 = "uid"
        const val EMAIL               = "email"
        const val NOME                = "nome"
        const val COGNOME             = "cognome"
        const val PHOTO_URL           = "photourl"
        const val ID_AZIENDA          = "idAzienda"
        const val MANAGER             = "isManager"
        const val RUOLO               = "ruolo"
        const val CODICE_FISCALE      = "codiceFiscale"
        const val DATA_NASCITA        = "dataNascita"
        const val DIPARTIMENTO        = "dipartimento"
        const val INDIRIZZO           = "indirizzo"
        const val LUOGO_NASCITA       = "luogoNascita"
        const val NUMERO_TELEFONO     = "numeroTelefono"
        const val POSIZIONE_LAVORATIVA= "posizioneLavorativa"
    }
}


