package com.bizsync.domain.repository

import com.bizsync.domain.model.Contratto

interface ContractLocalRepository {
    suspend fun deleteAll()
    suspend fun getContratto(employeeId: String): Contratto?
    suspend fun getContratti(): List<Contratto>
}