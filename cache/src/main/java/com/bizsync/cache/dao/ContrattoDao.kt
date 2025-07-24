package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Query
import com.bizsync.cache.entity.ContrattoEntity


@Dao
interface ContrattoDao {

    @Query("SELECT * FROM contratti WHERE idAzienda = :aziendaId")
    suspend fun deleteByAzienda(aziendaId: String): ContrattoEntity?


    @Query("DELETE FROM contratti")
    suspend fun deleteAll()

    @Query("SELECT * FROM contratti ")
    suspend fun getContratti(): List<ContrattoEntity>

}
