package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserRemoteRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRemoteRepository: UserRemoteRepository
) {
    suspend operator fun invoke(userId: String): Resource<User> {
        return userRemoteRepository.getUserById(userId)
    }
}