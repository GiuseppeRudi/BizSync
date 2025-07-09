package com.bizsync.backend.sync


import com.bizsync.backend.dto.UserDto
import com.bizsync.backend.repository.UserRepository
import com.bizsync.cache.dao.UserDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.hash.generateCacheHash
import com.bizsync.backend.hash.generateDomainHash
import com.bizsync.cache.entity.UserEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.model.User
import javax.inject.Inject


class SyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val hashStorage: HashStorage
) {

    suspend fun syncIfNeeded(idAzienda: String): Resource<List<UserEntity>> {
        return try {
            // 1. Controlla hash salvato
            val savedHash = hashStorage.getDipendentiHash(idAzienda)

            // 2. UNA SOLA chiamata Firebase
            println("🌐 Chiamata Firebase per azienda $idAzienda")
            val firebaseResult = userRepository.getDipendentiByAzienda(idAzienda)

            when (firebaseResult) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data
                    val currentHash = firebaseData.generateDomainHash()

                    // 3. Confronta hash
                    if (savedHash == null || savedHash != currentHash) {
                        // SYNC NECESSARIO
                        println("🔄 Sync necessario per azienda $idAzienda")
                        println("   Hash salvato: $savedHash")
                        println("   Hash corrente: $currentHash")

                        // Esegui sync con dati già ottenuti
                        performSyncWithData(idAzienda, firebaseData, currentHash)
                    } else {
                        println("✅ Cache aggiornata per azienda $idAzienda")
                    }

                    // 4. Restituisci cache aggiornata
                    val cachedEntities = userDao.getDipendenti(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Error -> {
                    println("❌ Errore Firebase, uso cache: ${firebaseResult.message}")
                    // Usa cache esistente in caso di errore di rete
                    val cachedEntities = userDao.getDipendenti(idAzienda)
                    Resource.Success(cachedEntities)
                }

                is Resource.Empty -> {
                    // Firebase vuoto → pulisci cache
                    userDao.deleteByAzienda(idAzienda)
                    hashStorage.saveDipendentiHash(idAzienda, "")
                    Resource.Success(emptyList())
                }
            }

        } catch (e: Exception) {
            println("🚨 Errore in syncIfNeeded: ${e.message}")
            // Fallback alla cache locale
            try {
                val cachedEntities = userDao.getDipendenti(idAzienda)
                Resource.Success(cachedEntities)
            } catch (cacheError: Exception) {
                Resource.Error("Errore sync e cache: ${e.message}")
            }
        }
    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return try {
            // Elimina hash salvato per forzare sync
            hashStorage.deleteDipendentiHash(idAzienda)

            // Esegui sync
            when (val result = syncIfNeeded(idAzienda)) {
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
        try {
            // Converti e salva in cache
            val entities = firebaseData.toEntityList()
            userDao.deleteByAzienda(idAzienda)
            userDao.insertAll(entities)

            // Salva nuovo hash
            hashStorage.saveDipendentiHash(idAzienda, newHash)

            println("✅ Sync completato - ${entities.size} dipendenti - hash: $newHash")

        } catch (e: Exception) {
            println("❌ Errore durante sync: ${e.message}")
            throw e
        }
    }

    /**
     * VALIDAZIONE CACHE
     */
    suspend fun validateCacheIntegrity(idAzienda: String): Boolean {
        return try {
            val savedHash = hashStorage.getDipendentiHash(idAzienda) ?: return false
            val cachedData = userDao.getDipendenti(idAzienda)
            val currentCacheHash = cachedData.generateCacheHash()

            val isValid = savedHash == currentCacheHash

            if (!isValid) {
                println("⚠️ Cache integrity check failed per azienda $idAzienda")
                println("   Hash salvato: $savedHash")
                println("   Hash cache: $currentCacheHash")
            }

            return isValid

        } catch (e: Exception) {
            false
        }
    }
}
