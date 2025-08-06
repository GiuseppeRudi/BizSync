package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserRemoteRepository
import javax.inject.Inject

class AddUserUseCase @Inject constructor(
    private val userRemoteRepository: UserRemoteRepository
) {
    suspend operator fun invoke(user: User, uid: String): Resource<User> {
        return try {
            val success = userRemoteRepository.addUser(user, uid)
            if (success) {
                Resource.Success(user.copy(uid = uid))
            } else {
                Resource.Error("Errore durante il salvataggio dell'utente")
            }
        } catch (e: Exception) {
            Resource.Error("Errore nella creazione dell'utente: ${e.message}")
        }
    }
}