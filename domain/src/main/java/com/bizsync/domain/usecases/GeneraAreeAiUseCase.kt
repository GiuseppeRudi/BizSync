package com.bizsync.domain.usecases

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.repository.OnBoardingPianificaRepository
import javax.inject.Inject

class GeneraAreeAiUseCase @Inject constructor(
    private val onBoardingPianificaRemoteRepository: OnBoardingPianificaRepository
) {
    suspend operator fun invoke(nomeAzienda: String): List<AreaLavoro> {
        return onBoardingPianificaRemoteRepository.setAreaAi(nomeAzienda)
    }
}