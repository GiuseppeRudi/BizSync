package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserSyncRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserSyncRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        idUser: String,
        forceRefresh: Boolean = false
    ): Resource<List<User>> {
        return userRepository.getDipendenti(idAzienda, idUser, forceRefresh)
    }
}