package com.bizsync.cache.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "turni")
data class TurnoEntity(
    @PrimaryKey val idDocumento: String,
    val nome: String,
    val giorno: Timestamp
)
