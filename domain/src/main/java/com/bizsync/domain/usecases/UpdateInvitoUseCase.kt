package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.repository.InviteRepository
import javax.inject.Inject

class UpdateInvitoUseCase @Inject constructor(
    private val invitRemoteRepository: InviteRepository
) {
    suspend operator fun invoke(invito: Invito): Resource<Unit> {
        return invitRemoteRepository.updateInvito(invito)
    }
}