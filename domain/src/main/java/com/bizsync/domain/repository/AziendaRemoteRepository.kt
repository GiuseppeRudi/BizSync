package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente

interface AziendaRemoteRepository {
    suspend fun creaAzienda(azienda: Azienda): Resource<String>
    suspend fun getAziendaById(idAzienda: String): Resource<Azienda>

    suspend fun addPianificaSetup(
        idAzienda: String,
        aree: List<AreaLavoro>,
        turni: List<TurnoFrequente>
    ): Resource<Unit>
    suspend fun updateAreeLavoro(idAzienda: String, aree: List<AreaLavoro>): Resource<Unit>
}