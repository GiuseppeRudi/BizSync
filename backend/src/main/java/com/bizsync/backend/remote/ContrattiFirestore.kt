package com.bizsync.backend.remote


object ContrattiFirestore {

    const val COLLECTION = "contratti"

    object Fields {
        const val ID = "id"
        const val ID_DIPENDENTE = "idDipendente"
        const val ID_AZIENDA = "idAzienda"
        const val EMAIL_DIPENDENTE = "emailDipendente"
        const val POSIZIONE_LAVORATIVA = "posizioneLavorativa"
        const val DIPARTIMENTO = "dipartimento"
        const val TIPO_CONTRATTO = "tipoContratto"
        const val ORE_SETTIMANALI = "oreSettimanali"
        const val SETTORE_AZIENDALE = "settoreAziendale"
        const val DATA_INIZIO = "dataInizio"
        const val CCNL_INFO = "ccnlInfo"  // assuming serialized as map/object
    }
}
