package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.TimbraturaRemoteRepository
import javax.inject.Inject

class VerificaTimbrnaturaUseCase @Inject constructor(
    private val timbraturaRemoteRepository: TimbraturaRemoteRepository
) {
    suspend operator fun invoke(idTimbratura: String): Resource<Unit> {
        return timbraturaRemoteRepository.verificaTimbratura(idTimbratura)
    }
}