package com.bizsync.backend.remote


object ChatFirestore {

    const val COLLECTION = "chats"
    const val MESSAGES_SUBCOLLECTION = "messages"

    object Fields {
        const val ID = "id"
        const val TIPO = "tipo"
        const val NOME = "nome"
        const val ID_AZIENDA = "idAzienda"
        const val DIPARTIMENTO = "dipartimento"
        const val PARTECIPANTI = "partecipanti"
        const val ULTIMO_MESSAGGIO = "ultimoMessaggio"
        const val ULTIMO_MESSAGGIO_TIMESTAMP = "ultimoMessaggioTimestamp"
        const val ULTIMO_MESSAGGIO_SENDER_ID = "ultimoMessaggioSenderId"
    }

    object TipoChat {
        const val GENERALE = "generale"
        const val DIPARTIMENTO = "dipartimento"
        const val PRIVATA = "privata"
    }

    object MessageFields {
        const val ID = "id"
        const val SENDER_ID = "senderId"
        const val TIMESTAMP = "timestamp"
        const val LETTO_DA = "lettoDa"
    }

    object MessageTypes {
        const val TEXT = "text"
        const val ANNOUNCEMENT = "announcement"
        const val SYSTEM = "system"
    }

}