package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserRemoteRepository
import javax.inject.Inject

class UpdateUserPersonalInfoUseCase @Inject constructor(
    private val userRemoteRepository: UserRemoteRepository
) {
    suspend operator fun invoke(user: User): Resource<User> {
        return try {
            val success = userRemoteRepository.updateUser(user, user.uid)
            if (success) {
                Resource.Success(user)
            } else {
                Resource.Error("Errore nell'aggiornamento del profilo")
            }
        } catch (e: Exception) {
            Resource.Error("Errore nell'aggiornamento profilo: ${e.message}")
        }
    }
}