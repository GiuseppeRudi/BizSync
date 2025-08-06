package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractRemoteRepository
import javax.inject.Inject

class GetContrattoByUserAndAziendaUseCase @Inject constructor(
    private val contractRemoteRepository: ContractRemoteRepository
) {
    suspend operator fun invoke(idUser: String, idAzienda: String): Resource<Contratto> {
        return contractRemoteRepository.getContrattoByUserAndAzienda(idUser, idAzienda)
    }
}
