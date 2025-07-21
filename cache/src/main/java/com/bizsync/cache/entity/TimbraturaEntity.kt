package com.bizsync.cache.entity

import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import java.time.LocalDateTime

@Entity(tableName = "timbrature")
data class TimbraturaEntity(
    @PrimaryKey
    val id: String,
    val idTurno: String,
    val idDipendente: String,
    val idAzienda: String,
    val idFirebase: String,

    val tipoTimbratura: TipoTimbratura,
    val dataOraTimbratura: LocalDateTime,
    val dataOraPrevista: LocalDateTime,

    val zonaLavorativa: ZonaLavorativa,

    val posizioneVerificata: Boolean,
    val distanzaDallAzienda: Double?,
    val dentroDellaTolleranza: Boolean,

    val statoTimbratura: StatoTimbratura,
    val minutiRitardo: Int,

    val note: String,
    val verificataDaManager: Boolean,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
