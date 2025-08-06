package com.bizsync.domain.usecases

import android.util.Log
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.DatabaseCleanupResult
import com.bizsync.domain.repository.AbsenceLocalRepository
import com.bizsync.domain.repository.ContractLocalRepository
import com.bizsync.domain.repository.TimbraturaLocalRepository
import com.bizsync.domain.repository.TurnoLocalRepository
import com.bizsync.domain.repository.UserLocalRepository
import javax.inject.Inject

class ClearDatabaseUseCase @Inject constructor(
    private val absenceLocalRepository: AbsenceLocalRepository,
    private val timbraturaLocalRepository: TimbraturaLocalRepository,
    private val turnoLocalRepository: TurnoLocalRepository,
    private val contractLocalRepository: ContractLocalRepository,
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(): Resource<DatabaseCleanupResult> {
        return try {
            val results = mutableMapOf<String, Boolean>()

            // ✅ Pulizia in ordine specifico per rispettare le foreign keys
            try {
                absenceLocalRepository.clearAll()
                results["absences"] = true
            } catch (e: Exception) {
                results["absences"] = false
                Log.e("CLEANUP_DEBUG", "Errore pulizia assenze: ${e.message}")
            }

            try {
                timbraturaLocalRepository.clearAll()
                results["timbrature"] = true
            } catch (e: Exception) {
                results["timbrature"] = false
                Log.e("CLEANUP_DEBUG", "Errore pulizia timbrature: ${e.message}")
            }

            try {
                turnoLocalRepository.clearAll()
                results["turni"] = true
            } catch (e: Exception) {
                results["turni"] = false
                Log.e("CLEANUP_DEBUG", "Errore pulizia turni: ${e.message}")
            }

            try {
                contractLocalRepository.deleteAll()
                results["contracts"] = true
            } catch (e: Exception) {
                results["contracts"] = false
                Log.e("CLEANUP_DEBUG", "Errore pulizia contratti: ${e.message}")
            }

            try {
                userLocalRepository.deleteAll()
                results["users"] = true
            } catch (e: Exception) {
                results["users"] = false
                Log.e("CLEANUP_DEBUG", "Errore pulizia utenti: ${e.message}")
            }

            val successCount = results.values.count { it }
            val totalCount = results.size

            Resource.Success(
                DatabaseCleanupResult(
                    cleanupResults = results,
                    totalTables = totalCount,
                    successfulCleanups = successCount,
                    isFullySuccessful = successCount == totalCount
                )
            )
        } catch (e: Exception) {
            Resource.Error("Errore durante la pulizia del database: ${e.message}")
        }
    }
}