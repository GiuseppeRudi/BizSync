package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bizsync.cache.entity.TurnoEntity

@Dao
interface TurnoDao {

//    @Query("SELECT * FROM turni WHERE idDipendente = :dipendenteId")
//    suspend fun getTurniByDipendente(dipendenteId: String): List<TurnoEntity>
//
//    @Query("SELECT * FROM turni WHERE settimana = :week AND idDipendente = :dipendenteId")
//    suspend fun getTurniByWeek(dipendenteId: String, week: Int): List<TurnoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turni: List<TurnoEntity>)

    @Query("DELETE FROM turni")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM turni")
    suspend fun count(): Int
}
