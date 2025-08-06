package com.bizsync.domain.repository

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente

interface OnBoardingPianificaRepository {
    suspend fun setAreaAi(nomeAzienda: String): List<AreaLavoro>
    suspend fun setTurniAi(nomeAzienda: String): List<TurnoFrequente>
}