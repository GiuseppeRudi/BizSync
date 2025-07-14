package com.bizsync.cache.entity



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.time.LocalDate

@Entity(tableName = "turni")
data class TurnoEntity(
    @PrimaryKey val idDocumento: String,
    val idAzienda : String ,
    val idDipendenti : List<String> ,
    val data : LocalDate,
    val nome: String,
    val orarioInizio: String,
    val orarioFine: String,
    val dipendente: String,
    val dipartimentoId: String,
    val note: String,
    val isConfermato: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
