package com.bizsync.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bizsync.cache.entity.AbsenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AbsenceDao {


    @Query("""
        SELECT * FROM absences 
        WHERE (startDate BETWEEN :startDate AND :endDate) 
           OR (endDate BETWEEN :startDate AND :endDate)
           OR (startDate <= :startDate AND endDate >= :endDate)
        ORDER BY startDate ASC
    """)
    fun getAbsencesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<AbsenceEntity>>

    @Query("SELECT * FROM absences WHERE startDate >= :fromDate ORDER BY startDate ASC")
    fun getAbsencesFrom(fromDate: LocalDate): Flow<List<AbsenceEntity>>

    @Query("DELETE FROM absences WHERE endDate < :beforeDate")
    suspend fun deleteAbsencesBefore(beforeDate: LocalDate)


    @Query("SELECT * FROM absences ORDER BY submittedDate DESC")
    suspend fun getAbsences(): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE idUser = :idUser ORDER BY submittedDate DESC")
    suspend fun getAbsencesByUser(idUser: String): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE idAzienda = :idAzienda AND status = :status ORDER BY submittedDate DESC")
    suspend fun getAbsencesByStatus(idAzienda: String, status: String): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE idUser = :idUser AND status = :status ORDER BY submittedDate DESC")
    suspend fun getUserAbsencesByStatus(idUser: String, status: String): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE idAzienda = :idAzienda AND type = :type ORDER BY submittedDate DESC")
    suspend fun getAbsencesByType(idAzienda: String, type: String): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE id = :id LIMIT 1")
    suspend fun getAbsenceById(id: String): AbsenceEntity?

    // Query per intervallo di date
    @Query("""
        SELECT * FROM absences 
        WHERE idAzienda = :idAzienda 
        AND submittedDate >= :startDate 
        AND submittedDate <= :endDate 
        ORDER BY submittedDate DESC
    """)
    suspend fun getAbsencesInDateRange(
        idAzienda: String,
        startDate: String,
        endDate: String
    ): List<AbsenceEntity>

    // Query per assenze pending (per manager)
    @Query("""
        SELECT * FROM absences 
        WHERE idAzienda = :idAzienda 
        AND status = 'PENDING' 
        ORDER BY submittedDate ASC
    """)
    suspend fun getPendingAbsences(idAzienda: String): List<AbsenceEntity>

    // Count queries per statistiche
    @Query("SELECT COUNT(*) FROM absences WHERE idUser = :idUser AND status = :status")
    suspend fun countAbsencesByUserAndStatus(idUser: String, status: String): Int

    @Query("SELECT COUNT(*) FROM absences WHERE idAzienda = :idAzienda AND status = 'PENDING'")
    suspend fun countPendingAbsences(idAzienda: String): Int

    // ==================== INSERT METHODS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(absence: AbsenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(absences: List<AbsenceEntity>)

    // ==================== UPDATE METHODS ====================

    @Update
    suspend fun update(absence: AbsenceEntity)

    @Query("""
        UPDATE absences 
        SET status = :status, 
            comments = :comments, 
            approver = :approver, 
            approvedDate = :approvedDate,
            lastUpdated = :lastUpdated
        WHERE id = :id
    """)
    suspend fun updateAbsenceStatus(
        id: String,
        status: String,
        comments: String?,
        approver: String?,
        approvedDate: String?,
        lastUpdated: Long = System.currentTimeMillis()
    )

    // ==================== DELETE METHODS ====================

    @Delete
    suspend fun delete(absence: AbsenceEntity)

    @Query("DELETE FROM absences WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM absences WHERE idAzienda = :idAzienda")
    suspend fun deleteByAzienda(idAzienda: String)

    @Query("""
    DELETE FROM absences 
    WHERE idAzienda = :idAzienda 
      AND startDate >= :startDate 
      AND endDate <= :endDate
""")
    suspend fun deleteByAziendaInDateRange(
        idAzienda: String,
        startDate: String, // formato "yyyy-MM-dd"
        endDate: String    // formato "yyyy-MM-dd"
    )

    @Query("""
    DELETE FROM absences
    WHERE endDate  < :endDate
""")
    suspend fun deleteOlderThanWeek(endDate: String)




    @Query("DELETE FROM absences WHERE idUser = :idUser")
    suspend fun deleteByUser(idUser: String)

    // Delete old records (cleanup)
    @Query("DELETE FROM absences WHERE submittedDate < :cutoffDate")
    suspend fun deleteOldAbsences(cutoffDate: String)

    // ==================== UTILITY METHODS ====================

    @Query("SELECT COUNT(*) FROM absences")
    suspend fun getTotalCount(): Int

    @Query("SELECT MAX(lastUpdated) FROM absences WHERE idAzienda = :idAzienda")
    suspend fun getLastUpdateTime(idAzienda: String): Long?

    @Query("DELETE FROM absences")
    suspend fun clearAll()

    // Advanced queries for reports
    @Query("""
        SELECT type, COUNT(*) as count 
        FROM absences 
        WHERE idAzienda = :idAzienda 
        AND status = 'APPROVED'
        AND submittedDate >= :startDate 
        AND submittedDate <= :endDate
        GROUP BY type
    """)
    suspend fun getAbsenceStatsByType(
        idAzienda: String,
        startDate: String,
        endDate: String
    ): List<AbsenceTypeStat>

    @Query("""
        SELECT idUser, submittedName, COUNT(*) as count 
        FROM absences 
        WHERE idAzienda = :idAzienda 
        AND status = 'APPROVED'
        AND submittedDate >= :startDate 
        AND submittedDate <= :endDate
        GROUP BY idUser, submittedName
        ORDER BY count DESC
    """)
    suspend fun getAbsenceStatsByUser(
        idAzienda: String,
        startDate: String,
        endDate: String
    ): List<UserAbsenceStat>
}

// ==================== DATA CLASSES PER STATISTICHE ====================

data class AbsenceTypeStat(
    val type: String,
    val count: Int
)

data class UserAbsenceStat(
    val idUser: String,
    val submittedName: String,
    val count: Int
)
