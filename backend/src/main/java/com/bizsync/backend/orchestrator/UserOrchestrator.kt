package com.bizsync.backend.orchestrator


import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.repository.UserRepository
import com.bizsync.backend.sync.SyncManager
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import javax.inject.Inject

class UserOrchestrator @Inject constructor(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val hashStorage: HashStorage,
    private val syncManager: SyncManager
) {

    suspend fun getDipendenti(idAzienda: String, forceRefresh: Boolean = false): Resource<List<User>> {
        return try {
            // 1. Se forceRefresh, forza il sync
            if (forceRefresh) {
                syncManager.forceSync(idAzienda)
            } else {
                // 2. Controlla se è necessario sincronizzare
                if (syncManager.shouldSync(idAzienda)) {
                    syncManager.performSync(idAzienda)
                } else {
                    // 3. Opzionale: Valida integrità cache
                    if (!syncManager.validateCacheIntegrity(idAzienda)) {
                        println("⚠️ Cache corrotta, forzando sync...")
                        syncManager.forceSync(idAzienda)
                    }
                }
            }

            // 4. Restituisci sempre dalla cache
            val cachedDipendenti = userDao.getDipendenti(idAzienda)
            Resource.Success(cachedDipendenti.toDomainList())

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore nel recupero dipendenti")
        }
    }

//    suspend fun createDipendente(dipendente: User): Resource<String> {
//        return try {
//            // 1. Crea su Firebase
//            when (val result = userRepository.createDipendente(dipendente.toDto())) {
//                is Resource.Success -> {
//                    // 2. Invalida hash per forzare sync al prossimo accesso
//                    hashStorage.deleteDipendentiHash(dipendente.idAzienda)
//
//                    // 3. Opzionale: Sync immediato
//                    syncManager.performSync(dipendente.idAzienda)
//
//                    Resource.Success(result.data)
//                }
//                else -> result
//            }
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Errore nella creazione dipendente")
//        }
//    }
//
//    suspend fun updateDipendente(dipendente: User): Resource<Unit> {
//        return try {
//            // 1. Aggiorna su Firebase
//            when (val result = userRepository.updateDipendente(dipendente.id, dipendente.toDto())) {
//                is Resource.Success -> {
//                    // 2. Invalida hash per forzare sync
//                    hashStorage.deleteDipendentiHash(dipendente.idAzienda)
//
//                    // 3. Sync immediato
//                    syncManager.performSync(dipendente.idAzienda)
//
//                    Resource.Success(Unit)
//                }
//                else -> result
//            }
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Errore nell'aggiornamento dipendente")
//        }
//    }
//
//    suspend fun deleteDipendente(id: String, idAzienda: String): Resource<Unit> {
//        return try {
//            // 1. Elimina da Firebase
//            when (val result = userRepository.deleteDipendente(id)) {
//                is Resource.Success -> {
//                    // 2. Invalida hash per forzare sync
//                    hashStorage.deleteDipendentiHash(idAzienda)
//
//                    // 3. Sync immediato
//                    syncManager.performSync(idAzienda)
//
//                    Resource.Success(Unit)
//                }
//                else -> result
//            }
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Errore nell'eliminazione dipendente")
//        }
//    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        return syncManager.forceSync(idAzienda)
    }
}