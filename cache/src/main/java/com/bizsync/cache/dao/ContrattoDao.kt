package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.domain.model.Contratto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ContrattoDao {

    @Query("SELECT * FROM contratti WHERE idDipendente = :dipendenteId ")
    suspend fun getContratto(dipendenteId: String): ContrattoEntity?


    @Query("SELECT * FROM contratti WHERE dataInizio <= :checkDate ")
    fun getContrattiAttivi(checkDate: LocalDate): Flow<List<ContrattoEntity>>

    @Query("DELETE FROM contratti WHERE dataInizio IS NOT NULL AND dataInizio < :beforeDate")
    suspend fun deleteContrattiScadutiPrima(beforeDate: LocalDate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contratti: List<ContrattoEntity>)

    @Query("SELECT * FROM contratti WHERE idAzienda = :aziendaId")
    suspend fun deleteByAzienda(aziendaId: String): ContrattoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contratto: ContrattoEntity)

    @Query("DELETE FROM contratti")
    suspend fun deleteAll()

    @Query("SELECT * FROM contratti ")
    suspend fun getContratti(): List<ContrattoEntity>

    @Query("SELECT COUNT(*) FROM contratti")
    suspend fun count(): Int
}
