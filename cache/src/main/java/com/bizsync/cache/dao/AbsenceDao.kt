package com.bizsync.cache.dao

import androidx.room.Dao
import androidx.room.Query
import com.bizsync.cache.entity.AbsenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AbsenceDao {


    @Query(
        """
        SELECT * FROM absences 
        WHERE (startDate BETWEEN :startDate AND :endDate) 
           OR (endDate BETWEEN :startDate AND :endDate)
           OR (startDate <= :startDate AND endDate >= :endDate)
        ORDER BY startDate ASC
    """
    )
    fun getAbsencesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<AbsenceEntity>>


    @Query("SELECT * FROM absences ORDER BY submittedDate DESC")
    suspend fun getAbsences(): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE idUser = :idUser ORDER BY submittedDate DESC")
    suspend fun getAbsencesByUser(idUser: String): List<AbsenceEntity>

    @Query(
        """
    DELETE FROM absences 
    WHERE idAzienda = :idAzienda 
      AND startDate >= :startDate 
      AND endDate <= :endDate
"""
    )
    suspend fun deleteByAziendaInDateRange(
        idAzienda: String,
        startDate: String, // formato "yyyy-MM-dd"
        endDate: String    // formato "yyyy-MM-dd"
    )

    @Query(
        """
    DELETE FROM absences
    WHERE endDate  < :endDate
"""
    )
    suspend fun deleteOlderThanWeek(endDate: String)


    @Query("DELETE FROM absences")
    suspend fun clearAll()

}