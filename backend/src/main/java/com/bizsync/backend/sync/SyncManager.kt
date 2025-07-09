package com.bizsync.backend.sync


import com.bizsync.backend.repository.UserRepository
import com.bizsync.cache.dao.UserDao
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.backend.hash.HashStorage
import com.bizsync.backend.hash.generateCacheHash
import com.bizsync.backend.hash.generateDomainHash
import com.bizsync.cache.mapper.toEntityList
import javax.inject.Inject


class SyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val hashStorage: HashStorage
) {

    suspend fun shouldSync(idAzienda: String): Boolean {
        return try {
            // 1. Ottieni hash salvato (ultimo sync)
            val savedHash = hashStorage.getDipendentiHash(idAzienda)

            // 2. Se non c'è hash salvato, è la prima volta → sync
            if (savedHash == null) return true

            // 3. Ottieni dati attuali da Firebase
            val firebaseResult = userRepository.getDipendentiByAzienda(idAzienda)

            // 4. Genera hash corrente da Firebase
            val currentFirebaseHash = when (firebaseResult) {
                is Resource.Success -> firebaseResult.data.generateDomainHash()
                is Resource.Error -> {
                    // Se Firebase non è raggiungibile, non fare sync
                    return false
                }
                is Resource.Empty -> ""
            }

            // 5. Confronta hash
            val hashChanged = savedHash != currentFirebaseHash

            // 6. Log per debug
            if (hashChanged) {
                println("🔄 Sync necessario per azienda $idAzienda")
                println("   Hash salvato: $savedHash")
                println("   Hash corrente: $currentFirebaseHash")
            }

            return hashChanged

        } catch (e: Exception) {
            // In caso di errore, non fare sync per evitare loop infiniti
            false
        }
    }

    suspend fun performSync(idAzienda: String): Resource<Unit> {
        return try {
            // 1. Ottieni dati da Firebase
            when (val firebaseResult = userRepository.getDipendentiByAzienda(idAzienda)) {
                is Resource.Success -> {
                    val firebaseData = firebaseResult.data

                    // 2. Genera hash dei nuovi dati
                    val newHash = firebaseData.generateDomainHash()

                    // 3. Aggiorna cache locale
                    val entities = firebaseData.toEntityList()
                    userDao.deleteByAzienda(idAzienda)
                    userDao.insertAll(entities)

                    // 4. Salva nuovo hash
                    hashStorage.saveDipendentiHash(idAzienda, newHash)

                    println("✅ Sync completato per azienda $idAzienda con hash: $newHash")
                    Resource.Success(Unit)
                }

                is Resource.Error -> {
                    Resource.Error(firebaseResult.message ?: "Errore durante il sync")
                }

                is Resource.Empty -> {
                    // Firebase vuoto → pulisci cache e hash
                    userDao.deleteByAzienda(idAzienda)
                    hashStorage.saveDipendentiHash(idAzienda, "")

                    Resource.Success(Unit)
                }
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Errore durante la sincronizzazione")
        }
    }

    suspend fun forceSync(idAzienda: String): Resource<Unit> {
        // Force sync elimina l'hash salvato e riforza il sync
        hashStorage.deleteDipendentiHash(idAzienda)
        return performSync(idAzienda)
    }

    suspend fun validateCacheIntegrity(idAzienda: String): Boolean {
        return try {

            // Verifica che l'hash della cache corrisponda a quello salvato
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
