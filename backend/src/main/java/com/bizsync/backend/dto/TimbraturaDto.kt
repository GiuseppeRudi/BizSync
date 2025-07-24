package com.bizsync.backend.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class TimbraturaDto(
    @get:Exclude
    val idFirebase: String = "",

    val idTurno: String = "",
    val idDipendente: String = "",
    val idAzienda: String = "",

    val tipoTimbratura: String = "",
    val dataOraTimbratura: Timestamp = Timestamp.now(),
    val dataOraPrevista: Timestamp = Timestamp.now(),
    val timestamp: Timestamp = Timestamp.now(),

    val zonaLavorativa: String = "",

    val posizioneVerificata: Boolean = false,
    val distanzaDallAzienda: Double? = null,
    val dentroDellaTolleranza: Boolean = false,

    val statoTimbratura: String = "",
    val minutiRitardo: Int = 0,

    val note: String = "",
    val verificataDaManager: Boolean = false,

    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
