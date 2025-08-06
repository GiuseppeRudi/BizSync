package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.repository.AziendaRemoteRepository
import javax.inject.Inject

class SalvaPianificaSetupUseCase @Inject constructor(
    private val aziendaRemoteRepository: AziendaRemoteRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        aree: List<AreaLavoro>,
        turni: List<TurnoFrequente>
    ): Resource<Unit> {
        return aziendaRemoteRepository.addPianificaSetup(idAzienda, aree, turni)
    }
}