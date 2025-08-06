package com.bizsync.cache.repository

import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContractLocalRepositoryImpl @Inject constructor(
    private val contrattoDao: ContrattoDao
) : ContractLocalRepository {

    override suspend fun deleteAll() {
        contrattoDao.deleteAll()
    }

    override suspend fun getContratto(employeeId: String): Contratto? {
        return contrattoDao.getContratto(employeeId)?.toDomain()
    }

    override suspend fun getContratti(): List<Contratto> {
        return contrattoDao.getContratti().toDomainList()
    }

}
