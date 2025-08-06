package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.repository.InviteRepository
import javax.inject.Inject

class SendInviteUseCase @Inject constructor(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(invito: Invito): Resource<Unit> {
        return try {
            inviteRepository.caricaInvito(invito)
        } catch (e: Exception) {
            Resource.Error("Errore nell'invio dell'invito: ${e.message}")
        }
    }
}
