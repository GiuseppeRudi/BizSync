package com.bizsync.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utenti")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val photourl: String = "",
    val idAzienda: String = "",
    val isManager: Boolean = false,
    val posizioneLavorativa: String = "",
    val dipartimento: String = ""
)

