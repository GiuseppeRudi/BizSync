package com.bizsync.sync.sync

import android.util.Log
import com.bizsync.backend.repository.UserRemoteRepositoryImpl
import com.bizsync.cache.dao.UserDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.cache.entity.UserEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.model.User
import com.bizsync.sync.extensions.generateDomainHash
import com.bizsync.sync.storage.UserHashStorage
import javax.inject.Inject

class SyncUserManager @Inject constructor(
    private val userRemoteRepositoryImpl: UserRemoteRepositoryImpl,
    private val userDao: UserDao,
    private val hashStorage: UserHashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String, idUser: String): Resource<List<UserEntity>> {
        val TAG = "DIPENDENTI_DEBUG"

        return try {
            val savedHash = hashStorage.getDipendentiHash(idAzienda)
            Log.d(TAG, " Chiamata Firebase per azienda $idAzienda")
            val firebaseResult = userRemoteRepositoryImpl.getDipendentiByAzienda(idAzienda, idUser)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    if (savedHash == null || savedHash != currentHash) {
                        Log.d(TAG, " Sync necessario per azienda $idAzienda")
                        Log.d(TAG, "   Hash salvato: $savedHash")
                        Log.d(TAG, "   Hash corrente: $currentHash")

                        performSyncWithData(idAzienda, firebaseData, currentHash)
                    } else {
                        Log.d(TAG, " Cache aggiornata per azienda $idAzienda")
                    }

                    val cachedEntities = userDao.getDipendenti()
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    Log.e(TAG, " Errore Firebase, uso cache: ${firebaseResult.message}")
                    val cachedEntities = userDao.getDipendenti()
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    Log.d(TAG, " Firebase vuoto, svuoto cache per azienda $idAzienda")
                    userDao.deleteByAzienda(idAzienda)
                    hashStorage.saveDipendentiHash(idAzienda, "")
                    Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            Log.e("DIPENDENTI_DEBUG", "🚨 Errore in syncIfNeeded: ${e.message}")
            return try {
                val cachedEntities = userDao.getDipendenti()
                Resource.Success(cachedEntities)
            } catch (cacheError: Exception) {
                Log.e("DIPENDENTI_DEBUG", "🛑 Errore nel fallback cache: ${cacheError.message}")
                Resource.Error("Errore sync e cache: ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String, idUser: String): Resource<Unit> {
        return try {
            hashStorage.deleteDipendentiHash(idAzienda)
            when (val result = syncIfNeeded(idAzienda, idUser)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Empty -> Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore force sync")
        }
    }

    private suspend fun performSyncWithData(
        idAzienda: String,
        firebaseData: List<User>,
        newHash: String
    ) {
        val TAG = "DIPENDENTI_DEBUG"
        try {
            val entities = firebaseData.toEntityList()
            userDao.deleteByAzienda(idAzienda)
            userDao.insertAll(entities)
            hashStorage.saveDipendentiHash(idAzienda, newHash)
            Log.d(TAG, " Sync completato - ${entities.size} dipendenti - hash: $newHash")
        } catch (e: Exception) {
            Log.e(TAG, " Errore durante sync: ${e.message}")
            throw e
        }
    }

}
