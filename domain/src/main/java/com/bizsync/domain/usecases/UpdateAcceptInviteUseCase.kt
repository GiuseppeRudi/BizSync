package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.repository.UserRemoteRepository
import javax.inject.Inject

class UpdateAcceptInviteUseCase @Inject constructor(
    private val userRemoteRepository: UserRemoteRepository
) {
    suspend operator fun invoke(invite: Invito, uid: String): Resource<Unit> {
        return userRemoteRepository.updateAcceptInvite(invite, uid)
    }
}
