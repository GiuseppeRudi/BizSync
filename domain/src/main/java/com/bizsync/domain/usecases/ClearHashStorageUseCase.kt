package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.HashRepository
import javax.inject.Inject

class ClearHashStorageUseCase @Inject constructor(
    private val hashRepository: HashRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return try {
            hashRepository.clearAllHashes()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore durante la pulizia preferenze: ${e.message}")
        }
    }
}