package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import java.time.LocalDateTime
import java.util.UUID

data class Timbratura(
    val id: String = UUID.randomUUID().toString(),
    val idTurno: String = "",
    val idDipendente: String = "",
    val idAzienda: String = "",
    val idFirebase: String = "",

    val tipoTimbratura: TipoTimbratura = TipoTimbratura.ENTRATA,
    val dataOraTimbratura: LocalDateTime = LocalDateTime.now(),
    val dataOraPrevista: LocalDateTime = LocalDateTime.now(),


    val zonaLavorativa: ZonaLavorativa = ZonaLavorativa.IN_SEDE,

    // Risultati della verifica
    val posizioneVerificata: Boolean = false,
    val distanzaDallAzienda: Double? = null, // in metri
    val dentroDellaTolleranza: Boolean = false,

    // Stato della timbratura
    val statoTimbratura: StatoTimbratura = StatoTimbratura.IN_ORARIO,
    val minutiRitardo: Int = 0,

    val note: String = "",
    val verificataDaManager: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun isAnomala(): Boolean {
        return !dentroDellaTolleranza || statoTimbratura == StatoTimbratura.RITARDO_GRAVE
    }
}
