package com.bizsync.domain.usecases


import com.bizsync.domain.repository.ContractRemoteRepository
import javax.inject.Inject

class SyncRecentContrattiUseCase @Inject constructor(
    private val contractRemoteRepository: ContractRemoteRepository
) {
    suspend operator fun invoke() {
        contractRemoteRepository.syncRecentContratti()
    }
}