package com.bizsync.cache.dao


import androidx.room.*
import com.bizsync.cache.entity.TimbraturaEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimbraturaDao {

    @Query("DELETE FROM timbrature")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM timbrature 
        WHERE DATE(dataOraTimbratura) BETWEEN :startDate AND :endDate 
        AND idDipendente = :userId 
        ORDER BY dataOraTimbratura DESC
    """)
    fun getTimbratureInRangeForUser(startDate: LocalDate, endDate: LocalDate, userId: String): Flow<List<TimbraturaEntity>>

    @Query("""
    SELECT * FROM timbrature 
    WHERE createdAt >= :startOfDay 
      AND createdAt <= :endOfDay 
      AND idDipendente = :userId 
    ORDER BY createdAt DESC
""")
    fun getTimbratureByDateAndUser(
        startOfDay: String,
        endOfDay: String,
        userId: String
    ): List<TimbraturaEntity>


    @Query("""
    SELECT * FROM timbrature 
    WHERE createdAt >= :startOfDay 
      AND createdAt <= :endOfDay 
    ORDER BY createdAt DESC
""")
    fun getTimbratureByDate(
        startOfDay: String,
        endOfDay: String
    ): Flow<List<TimbraturaEntity>>


    @Query("SELECT * FROM timbrature ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTimbrature(limit: Int): Flow<List<TimbraturaEntity>>


    @Query("SELECT * FROM timbrature WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TimbraturaEntity?

    @Query("SELECT * FROM timbrature WHERE idTurno = :idTurno AND idDipendente = :idDipendente ORDER BY dataOraTimbratura ASC")
    suspend fun getByTurnoAndDipendente(idTurno: String, idDipendente: String): List<TimbraturaEntity>


    @Query("""
    SELECT * FROM timbrature
    WHERE idDipendente = :idDipendente
    ORDER BY dataOraTimbratura DESC
    LIMIT :limit
""")
    suspend fun getUltimeTimbratureDipendente(
        idDipendente: String,
        limit: Int = 10
    ): List<TimbraturaEntity>

}