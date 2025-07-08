package com.bizsync.cache.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bizsync.cache.entity.ContrattoEntity

@Dao
interface ContrattoDao {

    @Query("SELECT * FROM contratti WHERE idDipendente = :dipendenteId AND idAzienda = :aziendaId")
    suspend fun getContratto(dipendenteId: String, aziendaId: String): ContrattoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contratto: ContrattoEntity)

    @Query("DELETE FROM contratti")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM contratti")
    suspend fun count(): Int
}
