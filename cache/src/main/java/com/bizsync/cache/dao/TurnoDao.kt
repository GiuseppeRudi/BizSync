package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import com.bizsync.cache.entity.TurnoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: TurnoEntity)

    @Update
    suspend fun update(turno: TurnoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turni: List<TurnoEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM turni WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM turni ORDER BY data ASC")
    suspend fun getTurni(): List<TurnoEntity>

    @Query("""
    SELECT * FROM turni 
    WHERE data BETWEEN :weekStart AND :weekEnd 
    AND isSynced = 0
""")
    suspend fun getTurniInRangeNonSync(
        weekStart: LocalDate,
        weekEnd: LocalDate
    ): List<TurnoEntity>


    @Query("""
        SELECT * FROM turni
        WHERE data BETWEEN :startDate AND :endDate AND isDeleted = 0
        ORDER BY data ASC
    """)
    suspend fun fetchTurniSettimana(startDate: LocalDate, endDate: LocalDate): List<TurnoEntity>


    @Query("SELECT * FROM turni WHERE data BETWEEN :start AND :end ORDER BY data ASC, orarioInizio ASC")
    suspend fun getTurniInRange(start: LocalDate, end: LocalDate): List<TurnoEntity>

    @Query("SELECT * FROM turni WHERE id = :id LIMIT 1")
    suspend fun getTurnoById(id: String): TurnoEntity?

    @Query("""
    DELETE FROM turni 
    WHERE idAzienda = :idAzienda 
      AND data BETWEEN :startDate AND :endDate
""")
    suspend fun deleteByAziendaForManager(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    )

    @Query("""
    DELETE FROM turni 
    WHERE data  < :endDate
""")
    suspend fun deleteOlderThan(endDate: LocalDate)



    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate 
        AND (:isSynced = 1 AND isSynced = 1) OR (:isSynced = 0 AND isSynced = 0)
        ORDER BY data ASC, orarioInizio ASC
    """)
    suspend fun getTurniSettimana(
        startDate: LocalDate,
        endDate: LocalDate,
        isSynced: Boolean
    ): List<TurnoEntity>


    @Query("""
    SELECT * FROM turni 
    WHERE idAzienda = :aziendaId 
      AND data >= :today
""")
    suspend fun getFutureShiftsFromToday(aziendaId: String, today: LocalDate): List<TurnoEntity>


    @Query("""
    SELECT * FROM turni 
    WHERE idAzienda = :aziendaId 
      AND data BETWEEN :start AND :end
""")
    suspend fun getPastShifts(aziendaId: String, start: LocalDate, end: LocalDate): List<TurnoEntity>


    @Query("SELECT * FROM turni WHERE data = :date ORDER BY orarioInizio ASC")
    fun getTurniByDateAndUser(date: LocalDate): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turni WHERE data = :date ORDER BY orarioInizio ASC")
    fun getTurniByDate(date: LocalDate): Flow<List<TurnoEntity>>


    @Query("DELETE FROM turni")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate
        ORDER BY data DESC, orarioInizio ASC
    """)
    fun getTurniInRangeForUser(startDate: LocalDate, endDate: LocalDate): Flow<List<TurnoEntity>>


    @Delete
    suspend fun deleteTurno(turno: TurnoEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoEntity)


    @Query("""
    UPDATE turni 
    SET idFirebase = :firebaseId, isSynced = :isSync 
    WHERE id = :id
""")
    suspend fun updateTurnoSyncStatus(id: String, firebaseId: String?, isSync: Boolean)


    suspend fun updateTurnoSyncStatus(turno: TurnoEntity) {
        updateTurnoSyncStatus(turno.id, turno.idFirebase, turno.isSynced)
    }

}
