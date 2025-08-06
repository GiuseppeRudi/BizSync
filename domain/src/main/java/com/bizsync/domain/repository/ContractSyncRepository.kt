package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto

interface ContractSyncRepository {
    suspend fun getContratti(idAzienda: String, forceRefresh: Boolean = false): Resource<List<Contratto>>
    suspend fun updateContratto(contratto: Contratto): Resource<String>
}