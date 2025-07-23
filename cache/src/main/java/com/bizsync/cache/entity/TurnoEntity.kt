package com.bizsync.cache.entity



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime


@Entity(tableName = "turni")
data class TurnoEntity(
    @PrimaryKey
    val id: String,
    val idFirebase: String,
    val idAzienda: String,
    val idDipendenti: List<String>,
    val titolo: String,
    val data: LocalDate,
    val orarioInizio: LocalTime,
    val orarioFine: LocalTime,
    val dipartimento: String,
    val zoneLavorativeJson: String = "{}", // Map<String, ZonaLavorativa> serializzata
    val noteJson: String = "[]", // List<Nota> serializzata
    val pauseJson: String = "[]", // List<Pausa> serializzata
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)