package com.bizsync.cache.dao

import androidx.room.Dao
import androidx.room.Query
import com.bizsync.cache.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM utenti")
    suspend fun getDipendenti(): List<UserEntity>

    @Query("SELECT * FROM utenti")
    suspend fun getDipendentiFull(): List<UserEntity>

    @Query("UPDATE utenti SET dipartimento = :dipartimento WHERE uid = :uid")
    suspend fun updateDipartimento(uid: String, dipartimento: String)

    @Query("DELETE FROM utenti")
    suspend fun deleteAll()

    @Query("DELETE FROM utenti WHERE idAzienda = :aziendaId")
    suspend fun deleteByAzienda(aziendaId: String)

}
