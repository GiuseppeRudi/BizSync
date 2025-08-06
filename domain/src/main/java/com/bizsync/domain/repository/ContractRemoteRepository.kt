package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.model.Contratto

interface ContractRemoteRepository {
    suspend fun generateCcnlInfo(
        posizioneLavorativa: String,
        dipartimento: String,
        settoreAziendale: String,
        tipoContratto: String,
        oreSettimanali: String
    ): Ccnlnfo

    suspend fun syncRecentContratti()
    suspend fun syncAllContratti()

    suspend fun getContrattoByUserAndAzienda(idUser: String, idAzienda: String): Resource<Contratto>

    suspend fun saveContract(contratto: Contratto): Resource<String>


}