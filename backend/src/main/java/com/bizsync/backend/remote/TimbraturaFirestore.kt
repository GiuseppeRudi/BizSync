package com.bizsync.backend.remote


object TimbraturaFirestore {

    const val COLLECTION = "timbrature"

    object Fields {
        const val ID = "id"
        const val ID_AZIENDA = "idAzienda"
        const val ID_DIPENDENTE = "idDipendente"

        const val DIPARTIMENTO = "dipartimento"
        const val DATA_ORA_TIMBRATURA = "dataOraTimbratura"
        const val TIMESTAMP = "timestamp"
        const val VERIFICATA_DA_MANAGER = "verificataDaManager"
    }


    object QueryLimits {
        const val MAX_RESULTS = 1000
    }

}