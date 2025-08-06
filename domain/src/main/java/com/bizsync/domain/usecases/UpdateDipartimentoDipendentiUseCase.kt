package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserSyncRepository
import javax.inject.Inject

class UpdateDipartimentoDipendentiUseCase @Inject constructor(
    private val userOrchestrator: UserSyncRepository // ✅ Usa orchestrator SYNC
) {
    suspend operator fun invoke(users: List<User>): Resource<Unit> {
        return try {
            userOrchestrator.updateDipartimentoDipendenti(users)
        } catch (e: Exception) {
            Resource.Error("Errore nell'aggiornamento dipartimenti: ${e.message}")
        }
    }
}