package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bizsync.cache.entity.TurnoEntity
import java.time.LocalDate


@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turni: List<TurnoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: TurnoEntity)

    @Update
    suspend fun update(turno: TurnoEntity)

    @Delete
    suspend fun delete(turno: TurnoEntity)

    // Recupera tutti i turni per una determinata azienda
    @Query("SELECT * FROM turni WHERE idAzienda = :idAzienda ORDER BY data ASC")
    suspend fun getTurniByAzienda(idAzienda: String): List<TurnoEntity>

    // Recupera tutti i turni di un determinato dipendente
    @Query("SELECT * FROM turni WHERE dipendente = :idDipendente ORDER BY data DESC")
    suspend fun getTurniByDipendente(idDipendente: String): List<TurnoEntity>


    @Query("""
        SELECT * FROM turni
        WHERE data BETWEEN :startDate AND :endDate
        ORDER BY data ASC
    """)
    suspend fun fetchTurniSettimana(startDate: LocalDate, endDate: LocalDate): List<TurnoEntity>

    // Recupera i turni di un dipartimento in un certo giorno
    @Query("SELECT * FROM turni WHERE dipartimentoId = :idDipartimento AND data = :giorno")
    suspend fun getTurniByDipartimentoAndData(idDipartimento: String, giorno: LocalDate): List<TurnoEntity>

    // Recupera i turni di un certo giorno
    @Query("SELECT * FROM turni WHERE data = :giorno ORDER BY orarioInizio ASC")
    suspend fun getTurniByGiorno(giorno: LocalDate): List<TurnoEntity>

    // Recupera i turni in un intervallo di date
    @Query("SELECT * FROM turni WHERE data BETWEEN :start AND :end ORDER BY data ASC, orarioInizio ASC")
    suspend fun getTurniInRange(start: LocalDate, end: LocalDate): List<TurnoEntity>

    // Recupera un turno per ID
    @Query("SELECT * FROM turni WHERE idDocumento = :id LIMIT 1")
    suspend fun getTurnoById(id: String): TurnoEntity?

    // Elimina tutti i turni per una certa azienda
    @Query("DELETE FROM turni WHERE idAzienda = :idAzienda")
    suspend fun deleteByAzienda(idAzienda: String)

    // Elimina tutti i turni per un certo dipartimento
    @Query("DELETE FROM turni WHERE dipartimentoId = :dipartimentoId")
    suspend fun deleteByDipartimento(dipartimentoId: String)
}
