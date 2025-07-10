package com.bizsync.backend.orchestrator


import android.util.Log
import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.repository.ContractRepository
import com.bizsync.backend.sync.SyncContrattoManager
import com.bizsync.backend.sync.SyncUserManager
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import javax.inject.Inject

class ContrattoOrchestrator @Inject constructor(
    private val contrattoRepository: ContractRepository,
    private val contrattoDao: ContrattoDao,
    private val hashStorage: HashStorage,
    private val syncContrattoManager: SyncContrattoManager
) {

    suspend fun getContratti(idAzienda: String, forceRefresh: Boolean = false): Resource<List<Contratto>> {
        return try {
            if (forceRefresh) {
                Log.d("CONTRATTI_DEBUG", "🔄 Force sync attivato per azienda $idAzienda")

                val syncResult = syncContrattoManager.forceSync(idAzienda)
                if (syncResult is Resource.Error) {
                    Log.e("CONTRATTI_DEBUG", "❌ Errore durante forceSync: ${syncResult.message}")
                }

                val cachedEntities = contrattoDao.getContratti(idAzienda)
                Log.d("CONTRATTI_DEBUG", "📦 Recuperati ${cachedEntities.size} contratti dalla cache dopo forceSync")
                Resource.Success(cachedEntities.toDomainList())
            } else {
                Log.d("CONTRATTI_DEBUG", "⚙️ Sync intelligente per azienda $idAzienda")

                when (val result = syncContrattoManager.syncIfNeeded(idAzienda)) {
                    is Resource.Success -> {
                        val domainContratti = contrattoDao.getContratti(idAzienda).toDomainList()
                        Log.d("CONTRATTI_DEBUG", "✅ Sync completato, contratti recuperati dalla cache: ${domainContratti.size}")
                        Resource.Success(domainContratti)
                    }
                    is Resource.Error -> {
                        Log.e("CONTRATTI_DEBUG", "❌ Errore sync intelligente: ${result.message}")
                        Resource.Error(result.message)
                    }
                    is Resource.Empty -> {
                        Log.d("CONTRATTI_DEBUG", "📭 Nessun contratto disponibile da Firebase")
                        Resource.Success(emptyList())
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("CONTRATTI_DEBUG", "🚨 Errore imprevisto nel recupero contratti: ${e.message}")
            Resource.Error(e.message ?: "Errore nel recupero contratti")
        }
    }
    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return syncContrattoManager.forceSync(idAzienda)
    }
}
