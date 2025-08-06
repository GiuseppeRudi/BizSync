package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserLocalRepository
import javax.inject.Inject

class LoadDipendentiAziendaUseCase @Inject constructor(
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(): Resource<List<User>> {
        return try {
            val dipendenti = userLocalRepository.getDipendentiFull()
            Resource.Success(dipendenti)
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento dipendenti: ${e.message}")
        }
    }
}