package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.bizsync.domain.repository.UserRemoteRepository
import javax.inject.Inject

class AggiornaAziendaUserUseCase @Inject constructor(
    private val userRemoteRepository: UserRemoteRepository
) {
    suspend operator fun invoke(idAzienda: String, idUtente: String, ruolo: RuoliAzienda): Resource<Unit> {
        return userRemoteRepository.aggiornaAzienda(idAzienda, idUtente, ruolo)
    }
}