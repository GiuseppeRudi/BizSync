package com.bizsync.cache.entity



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bizsync.domain.constants.enumClass.EsitoTurno
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "turni")
data class TurnoEntity(

    @PrimaryKey
    val id: String,

    val idFirebase : String,
    val idAzienda : String,
    val idDipendenti : List<String>,

    val titolo: String,

    val data : LocalDate,
    val orarioInizio: LocalTime,
    val orarioFine: LocalTime,

    val dipartimentoId: String,
    val note: String,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,


    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
