package com.bizsync.sync.orchestrator


import android.util.Log
import com.bizsync.backend.mapper.toDto
import com.bizsync.backend.repository.UserRemoteRepositoryImpl
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserSyncRepository
import com.bizsync.sync.sync.SyncUserManager
import javax.inject.Inject

class UserOrchestrator @Inject constructor(
    private val userDao: UserDao,
    private val syncUserManager: SyncUserManager,
    private val userRemoteRepositoryImpl: UserRemoteRepositoryImpl
) : UserSyncRepository {


    override suspend fun updateDipartimentoDipendenti(users: List<User>): Resource<Unit> {
        return try {
            Log.d("USER_ORCHESTRATOR", "=== INIZIO AGGIORNAMENTO DIPARTIMENTI ===")
            Log.d("USER_ORCHESTRATOR", "Utenti da aggiornare: ${users.size}")

            // ✅ Step 1: Aggiorna Firebase (BACKEND)
            Log.d("USER_ORCHESTRATOR", "Step 1: Aggiornamento Firebase...")

            userRemoteRepositoryImpl.updateDipartimentoDipendenti(users)


            // ✅ Step 2: Aggiorna Cache (CACHE)
            Log.d("USER_ORCHESTRATOR", "Step 2: Aggiornamento Cache locale...")
            users.forEach { user ->
                // Aggiorna Room direttamente
                userDao.updateDipartimento(user.uid, user.dipartimento)
                Log.d("USER_ORCHESTRATOR", "✅ Cache aggiornata per utente: ${user.uid}")
            }

            Log.d("USER_ORCHESTRATOR", "✅ Aggiornamento dipartimenti completato con successo")
            Resource.Success(Unit)

        } catch (e: Exception) {
            Log.e("USER_ORCHESTRATOR", "❌ Errore aggiornamento dipartimenti: ${e.message}", e)
            Resource.Error("Errore aggiornamento dipartimenti: ${e.message}")
        }
    }

    override suspend fun getDipendenti(idAzienda: String, idUser : String, forceRefresh: Boolean): Resource<List<User>> {
        return try {
            if (forceRefresh) {
                // Force sync
                syncUserManager.forceSync(idAzienda, idUser)
                val cachedEntities = userDao.getDipendenti()
                Resource.Success(cachedEntities.toDomainList())
            } else {
                // Sync intelligente con UNA SOLA chiamata Firebase
                when (val result = syncUserManager.syncIfNeeded(idAzienda, idUser)) {
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

}