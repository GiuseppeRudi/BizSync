package com.bizsync.sync.sync

import android.util.Log
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.backend.repository.ContractRemoteRepositoryImpl
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.model.Contratto
import com.bizsync.sync.extensions.generateDomainHash
import com.bizsync.sync.storage.ContrattiHashStorage
import javax.inject.Inject

class SyncContrattoManager @Inject constructor(
    private val contrattoRepository: ContractRemoteRepositoryImpl,
    private val contrattoDao: ContrattoDao,
    private val hashStorage: ContrattiHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String): Resource<List<ContrattoEntity>> {
        return try {
            val savedHash = hashStorage.getContrattiHash(idAzienda)

            Log.d("CONTRATTI_DEBUG", "🌐 Chiamata Firebase per contratti azienda $idAzienda")
            val firebaseResult = contrattoRepository.getContrattiByAzienda(idAzienda)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d("CONTRATTI_DEBUG", " Sync contratti necessario per azienda $idAzienda")
                        Log.d("CONTRATTI_DEBUG", "   Hash salvato: $savedHash")
                        Log.d("CONTRATTI_DEBUG", "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash)
                    } else {
                        Log.d("CONTRATTI_DEBUG", " CACHE ANCORA VALIDA NON CE DA AGGIORNARE  $idAzienda")
                    }

                    val cachedEntities = contrattoDao.getContratti()
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e("CONTRATTI_DEBUG", " Errore Firebase contratti, uso cache: ${firebaseResult.message}")
                    val cachedEntities = contrattoDao.getContratti()
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    contrattoDao.deleteByAzienda(idAzienda)
                    hashStorage.saveContrattiHash(idAzienda, "")
                    Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            Log.e("CONTRATTI_DEBUG", " Errore in syncIfNeeded (contratti): ${e.message}")
            try {
                val cachedEntities = contrattoDao.getContratti()
                Resource.Success(cachedEntities)
            } catch (e: Exception) {
                Resource.Error("Errore sync e cache (contratti): ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return try {
            hashStorage.deleteContrattiHash(idAzienda)

            when (val result = syncIfNeeded(idAzienda)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Empty -> Resource.Success(Unit)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore force sync (contratti)")
        }
    }

    private suspend fun performSyncWithData(
        idAzienda: String,
        firebaseData: List<Contratto>,
        newHash: String
    ) {
        try {
            val entities = firebaseData.toEntityList()
            contrattoDao.deleteByAzienda(idAzienda)
            contrattoDao.insertAll(entities)

            hashStorage.saveContrattiHash(idAzienda, newHash)

            Log.d("CONTRATTI_DEBUG", " Sync contratti completato - ${entities.size} contratti - hash: $newHash")

        } catch (e: Exception) {
            Log.e("CONTRATTI_DEBUG", "Errore durante sync contratti: ${e.message}")
            throw e
        }
    }

}
