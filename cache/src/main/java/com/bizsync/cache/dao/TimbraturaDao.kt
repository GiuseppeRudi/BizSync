package com.bizsync.cache.dao


import androidx.room.*
import com.bizsync.cache.entity.TimbraturaEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface TimbraturaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timbrature: List<TimbraturaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timbratura: TimbraturaEntity)

    @Update
    suspend fun update(timbratura: TimbraturaEntity)

    @Delete
    suspend fun delete(timbratura: TimbraturaEntity)

    @Query("SELECT * FROM timbrature WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TimbraturaEntity?

    @Query("SELECT * FROM timbrature WHERE idTurno = :idTurno AND idDipendente = :idDipendente ORDER BY dataOraTimbratura ASC")
    suspend fun getByTurnoAndDipendente(idTurno: String, idDipendente: String): List<TimbraturaEntity>



    @Query("SELECT * FROM timbrature WHERE idTurno = :idTurno ORDER BY dataOraTimbratura ASC")
    suspend fun getByTurno(idTurno: String): List<TimbraturaEntity>

    @Query("SELECT * FROM timbrature WHERE idDipendente = :idDipendente ORDER BY dataOraTimbratura DESC")
    suspend fun getByDipendente(idDipendente: String): List<TimbraturaEntity>

    @Query("""
        SELECT * FROM timbrature 
        WHERE idDipendente = :idDipendente 
        AND DATE(dataOraTimbratura) = :data
        ORDER BY dataOraTimbratura ASC
    """)
    suspend fun getByDipendenteAndData(idDipendente: String, data: LocalDate): List<TimbraturaEntity>

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


    @Query("""
        SELECT * FROM timbrature 
        WHERE idAzienda = :idAzienda 
        AND DATE(dataOraTimbratura) BETWEEN :startDate AND :endDate
        ORDER BY dataOraTimbratura DESC
    """)
    suspend fun getByAziendaAndDateRange(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TimbraturaEntity>

    @Query("""
        SELECT * FROM timbrature 
        WHERE idAzienda = :idAzienda 
        AND verificataDaManager = 0
        ORDER BY dataOraTimbratura DESC
    """)
    suspend fun getTimbraturedaVerificare(idAzienda: String): List<TimbraturaEntity>

    @Query("""
        SELECT * FROM timbrature 
        WHERE idAzienda = :idAzienda 
        AND (dentroDellaTolleranza = 0 OR statoTimbratura != 'IN_ORARIO')
        ORDER BY dataOraTimbratura DESC
    """)
    suspend fun getTimbratureAnomale(idAzienda: String): List<TimbraturaEntity>

    @Query("UPDATE timbrature SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE timbrature SET idFirebase = :firebaseId, isSynced = 1 WHERE id = :id")
    suspend fun updateFirebaseId(id: String, firebaseId: String)

    @Query("SELECT * FROM timbrature WHERE isSynced = 0")
    suspend fun getUnsyncedTimbrature(): List<TimbraturaEntity>
}