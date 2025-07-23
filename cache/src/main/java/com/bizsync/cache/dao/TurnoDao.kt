package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Query("SELECT EXISTS(SELECT 1 FROM turni WHERE id = :id)")
    suspend fun exists(id: String): Boolean

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

//    // Recupera tutti i turni di un determinato dipendente
//    @Query("SELECT * FROM turni WHERE dipendente = :idDipendente ORDER BY data DESC")
//    suspend fun getTurniByDipendente(idDipendente: String): List<TurnoEntity>


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
    @Query("SELECT * FROM turni WHERE id = :id LIMIT 1")
    suspend fun getTurnoById(id: String): TurnoEntity?

    // Elimina tutti i turni per una certa azienda
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


    // Elimina tutti i turni per un certo dipartimento
    @Query("DELETE FROM turni WHERE dipartimentoId = :dipartimentoId")
    suspend fun deleteByDipartimento(dipartimentoId: String)

    /**
     * Recupera i turni di una settimana specifica con filtro di sincronizzazione
     * @param startDate Data di inizio settimana
     * @param endDate Data di fine settimana
     * @param isSynced Filtro per stato di sincronizzazione (true = sincronizzati, false = non sincronizzati)
     * @return Lista di TurnoEntity
     */
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

    /**
     * Recupera tutti i turni di una settimana (sincronizzati e non)
     * @param startDate Data di inizio settimana
     * @param endDate Data di fine settimana
     * @return Lista di TurnoEntity
     */
    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate 
        ORDER BY data ASC, orarioInizio ASC
    """)
    suspend fun getAllTurniSettimana(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TurnoEntity>

    /**
     * Recupera i turni non sincronizzati di una settimana
     * @param startDate Data di inizio settimana
     * @param endDate Data di fine settimana
     * @return Lista di TurnoEntity non sincronizzati
     */
    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate 
        AND isSynced = 0
        ORDER BY data ASC, orarioInizio ASC
    """)
    suspend fun getTurniNonSincronizzati(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TurnoEntity>

    /**
     * Recupera i turni eliminati (marcati per eliminazione)
     * @param startDate Data di inizio settimana
     * @param endDate Data di fine settimana
     * @return Lista di TurnoEntity eliminati
     */
    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate 
        AND isDeleted = 1
        ORDER BY data ASC, orarioInizio ASC
    """)
    suspend fun getTurniEliminati(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TurnoEntity>



    @Query("SELECT * FROM turni WHERE data >= :fromDate ORDER BY data ASC")
    fun getTurniFrom(fromDate: LocalDate): Flow<List<TurnoEntity>>

    @Query("DELETE FROM turni WHERE data < :beforeDate")
    suspend fun deleteTurniBefore(beforeDate: LocalDate)


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


    // da sistemare
    @Query("SELECT * FROM turni WHERE data = :date ORDER BY orarioInizio ASC")
    fun getTurniByDateAndUser(date: LocalDate): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turni WHERE data = :date ORDER BY orarioInizio ASC")
    fun getTurniByDate(date: LocalDate): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turni WHERE data BETWEEN :startDate AND :endDate ORDER BY data ASC, orarioInizio ASC")
    fun getTurniInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TurnoEntity>>

    // Turni passati da una data a un'altra
    @Query("""
    SELECT * FROM turni 
    WHERE idDipendenti = :employeeId 
      AND data BETWEEN :startDate AND :endDate
    ORDER BY data DESC
""")
    suspend fun getEmployeePastShifts(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate = LocalDate.now()
    ): List<TurnoEntity>

    @Query("DELETE FROM turni")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM turni 
        WHERE data BETWEEN :startDate AND :endDate
        ORDER BY data DESC, orarioInizio ASC
    """)
    fun getTurniInRangeForUser(startDate: LocalDate, endDate: LocalDate): Flow<List<TurnoEntity>>

    /**
     * Aggiorna un turno esistente
     * @param turno TurnoEntity da aggiornare
     */
    @Update
    suspend fun updateTurno(turno: TurnoEntity)

    /**
     * Elimina fisicamente un turno dal database
     * @param turno TurnoEntity da eliminare
     */
    @Delete
    suspend fun deleteTurno(turno: TurnoEntity)

    /**
     * Inserisce un nuovo turno
     * @param turno TurnoEntity da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoEntity)

    /**
     * Marca un turno come eliminato (soft delete)
     * @param turnoId ID del turno da marcare come eliminato
     */
    @Query("UPDATE turni SET isDeleted = 1, isSynced = 0 WHERE id = :turnoId")
    suspend fun markTurnoAsDeleted(turnoId: String)

    /**
     * Aggiorna lo stato di sincronizzazione di un turno
     * @param turnoId ID del turno
     * @param isSynced Nuovo stato di sincronizzazione
     * @param firebaseId ID del documento Firebase (opzionale)
     */
    @Query("""UPDATE turni SET isSynced = :isSynced, idFirebase = :firebaseId WHERE id = :turnoId""")
    suspend fun updateSyncStatus(
        turnoId: String,
        isSynced: Boolean,
        firebaseId: String? = null
    )

    /**
     * Aggiorna il Firebase ID di un turno
     * @param turnoId ID del turno
     * @param firebaseId ID del documento Firebase
     */
    @Query("UPDATE turni SET idFirebase = :firebaseId WHERE id = :turnoId")
    suspend fun updateFirebaseId(turnoId: String, firebaseId: String)



    @Query("""
    UPDATE turni 
    SET idFirebase = :firebaseId, isSynced = :isSync 
    WHERE id = :id
""")
    suspend fun updateTurnoSyncStatus(id: String, firebaseId: String?, isSync: Boolean)


    // funzione helper opzionale
    suspend fun updateTurnoSyncStatus(turno: TurnoEntity) {
        updateTurnoSyncStatus(turno.id, turno.idFirebase, turno.isSynced)
    }



    /**
     * Conta i turni non sincronizzati
     * @return Numero di turni non sincronizzati
     */
    @Query("SELECT COUNT(*) FROM turni WHERE isSynced = 0")
    suspend fun countTurniNonSincronizzati(): Int

    /**
     * Conta i turni eliminati
     * @return Numero di turni eliminati
     */
    @Query("SELECT COUNT(*) FROM turni WHERE isDeleted = 1")
    suspend fun countTurniEliminati(): Int

    /**
     * Verifica se ci sono modifiche non sincronizzate
     * @return True se ci sono modifiche non sincronizzate
     */
    @Query("SELECT COUNT(*) > 0 FROM turni WHERE isSynced = 0")
    suspend fun hasUnsyncedChanges(): Boolean
}
