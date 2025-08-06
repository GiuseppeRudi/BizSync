package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.repository.ContractRemoteRepository
import javax.inject.Inject

class GenerateContractInfoUseCase @Inject constructor(
    private val contractRepository: ContractRemoteRepository
) {
    suspend operator fun invoke(
        posizioneLavorativa: String,
        dipartimento: String,
        settoreAziendale: String,
        tipoContratto: String,
        oreSettimanali: String
    ): Resource<Ccnlnfo> {
        return try {
            val ccnlInfo = contractRepository.generateCcnlInfo(
                posizioneLavorativa,
                dipartimento,
                settoreAziendale,
                tipoContratto,
                oreSettimanali
            )
            Resource.Success(ccnlInfo)
        } catch (e: Exception) {
            Resource.Error("Errore nella generazione informazioni CCNL: ${e.message}")
        }
    }
}
