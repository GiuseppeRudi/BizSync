package com.bizsync.backend.orchestrator

import com.bizsync.backend.sync.AbsenceSyncManager
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.bizsync.cache.mapper.toDomainList
import java.time.DayOfWeek
import java.time.LocalDate

@Singleton
class AbsenceOrchestrator @Inject constructor(
    private val absenceSyncManager: AbsenceSyncManager,
    private val absenceDao: AbsenceDao
) {


    suspend fun deleteOldCachedData(today: LocalDate = LocalDate.now()) {
        val cutoffDate = today.minusDays(90)
        val endOfWeek = cutoffDate.with(DayOfWeek.SUNDAY)
        absenceDao.deleteOlderThanWeek( endOfWeek.toString())
    }

    suspend fun getAbsences(idAzienda: String, idEmployee : String? , forceRefresh: Boolean = false): Resource<List<Absence>> {
        return try {
            if (forceRefresh) {
                Log.d("ABSENCE_ORCH", "🔄 Force sync attivato per azienda $idAzienda")

                val syncResult = absenceSyncManager.forceSync(idAzienda, idEmployee)
                if (syncResult is Resource.Error) {
                    Log.e("ABSENCE_ORCH", "❌ Errore durante forceSync: ${syncResult.message}")
                }

                // Recupera dalla cache (dao) dopo il force sync
                val cachedEntities = absenceDao.getAbsencesByAzienda(idAzienda)  // Assumi che questa funzione esista
                Log.d("ABSENCE_ORCH", "📦 Recuperate ${cachedEntities.size} assenze dalla cache dopo forceSync")
                Resource.Success(cachedEntities.toDomainList())

            } else {
                Log.d("ABSENCE_ORCH", "⚙️ Sync intelligente per azienda $idAzienda")

                when (val result = absenceSyncManager.syncIfNeeded(idAzienda,idEmployee)) {
                    is Resource.Success -> {
                        // Prendi i dati direttamente dalla cache dopo il sync intelligente
                        val cachedEntities = absenceDao.getAbsencesByAzienda(idAzienda)
                        val domainAbsences = cachedEntities.toDomainList()
                        Log.d("ABSENCE_ORCH", "✅ Sync completato, assenze recuperate dalla cache: ${domainAbsences.size}")
                        Resource.Success(domainAbsences)
                    }
                    is Resource.Error -> {
                        Log.e("ABSENCE_ORCH", "❌ Errore sync intelligente: ${result.message}")
                        Resource.Error(result.message)
                    }
                    is Resource.Empty -> {
                        Log.d("ABSENCE_ORCH", "📭 Nessuna assenza disponibile da Firebase")
                        Resource.Success(emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ABSENCE_ORCH", "🚨 Errore imprevisto nel recupero assenze: ${e.message}")
            Resource.Error(e.message ?: "Errore nel recupero assenze")
        }
    }

}