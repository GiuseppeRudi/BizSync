package com.bizsync.domain.usecases

import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.repository.OnBoardingPianificaRepository
import javax.inject.Inject

class GeneraTurniAiUseCase @Inject constructor(
    private val onBoardingPianificaRemoteRepository: OnBoardingPianificaRepository
) {
    suspend operator fun invoke(nomeAzienda: String): List<TurnoFrequente> {
        return onBoardingPianificaRemoteRepository.setTurniAi(nomeAzienda)
    }
}
