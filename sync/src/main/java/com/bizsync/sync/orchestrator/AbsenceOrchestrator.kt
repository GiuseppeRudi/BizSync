package com.bizsync.sync.orchestrator

import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.repository.AbsenceSyncRepository
import com.bizsync.sync.sync.SyncAbsenceManager
import java.time.DayOfWeek
import java.time.LocalDate

@Singleton
class AbsenceOrchestrator @Inject constructor(
    private val syncAbsenceManager: SyncAbsenceManager,
    private val absenceDao: AbsenceDao
)  : AbsenceSyncRepository {


    override suspend fun deleteOldCachedData(currentDate: LocalDate) {
        val cutoffDate = currentDate.minusDays(90)
        val endOfWeek = cutoffDate.with(DayOfWeek.SUNDAY)
        absenceDao.deleteOlderThanWeek( endOfWeek.toString())
    }

    override suspend fun getAbsences(idAzienda: String, idEmployee : String?, forceRefresh: Boolean): Resource<List<Absence>> {
        return try {
            if (forceRefresh) {
                Log.d("ABSENCE_ORCH", "Force sync attivato per azienda $idAzienda")

                val syncResult = syncAbsenceManager.forceSync(idAzienda, idEmployee)
                if (syncResult is Resource.Error) {
                    Log.e("ABSENCE_ORCH", "Errore durante forceSync: ${syncResult.message}")
                }

                val cachedEntities = absenceDao.getAbsences()
                Log.d("ABSENCE_ORCH", "Recuperate ${cachedEntities.size} assenze dalla cache dopo forceSync")
                Resource.Success(cachedEntities.toDomainList())

            } else {
                Log.d("ABSENCE_ORCH", "Sync intelligente per azienda $idAzienda")

                when (val result = syncAbsenceManager.syncIfNeeded(idAzienda,idEmployee)) {
                    is Resource.Success -> {
                        val cachedEntities = absenceDao.getAbsences()
                        val domainAbsences = cachedEntities.toDomainList()
                        Log.d("ABSENCE_ORCH", " Sync completato, assenze recuperate dalla cache: ${domainAbsences.size}")
                        Resource.Success(domainAbsences)
                    }
                    is Resource.Error -> {
                        Log.e("ABSENCE_ORCH", " Errore sync intelligente: ${result.message}")
                        Resource.Error(result.message)
                    }
                    is Resource.Empty -> {
                        Log.d("ABSENCE_ORCH", " Nessuna assenza disponibile da Firebase")
                        Resource.Success(emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ABSENCE_ORCH", " Errore imprevisto nel recupero assenze: ${e.message}")
            Resource.Error(e.message ?: "Errore nel recupero assenze")
        }
    }

}