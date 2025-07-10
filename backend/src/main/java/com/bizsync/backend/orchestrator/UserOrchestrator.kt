package com.bizsync.backend.orchestrator


import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.repository.UserRepository
import com.bizsync.backend.sync.SyncUserManager
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import javax.inject.Inject

class UserOrchestrator @Inject constructor(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val hashStorage: HashStorage,
    private val syncUserManager: SyncUserManager
) {

    suspend fun getDipendenti(idAzienda: String, forceRefresh: Boolean = false): Resource<List<User>> {
        return try {
            if (forceRefresh) {
                // Force sync
                syncUserManager.forceSync(idAzienda)
                val cachedEntities = userDao.getDipendenti(idAzienda)
                Resource.Success(cachedEntities.toDomainList())
            } else {
                // Sync intelligente con UNA SOLA chiamata Firebase
                when (val result = syncUserManager.syncIfNeeded(idAzienda)) {
                    is Resource.Success -> {
                        val domainUsers = result.data.toDomainList()
                        Resource.Success(domainUsers)
                    }
                    is Resource.Error -> result.let { Resource.Error(it.message) }
                    is Resource.Empty -> Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore nel recupero dipendenti")
        }
    }


    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return syncUserManager.forceSync(idAzienda)
    }
}