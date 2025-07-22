package com.bizsync.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bizsync.cache.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM utenti WHERE idAzienda = :aziendaId")
    suspend fun getDipendenti(aziendaId: String): List<UserEntity>

    @Query("SELECT * FROM utenti")
    suspend fun getDipendentiFull(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM utenti")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM utenti")
    suspend fun count(): Int

    @Query("DELETE FROM utenti WHERE idAzienda = :aziendaId")
    suspend fun deleteByAzienda(aziendaId: String)

}
