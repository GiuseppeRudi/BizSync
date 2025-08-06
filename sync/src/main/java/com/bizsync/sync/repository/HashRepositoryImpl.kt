package com.bizsync.sync.repository

import com.bizsync.domain.repository.HashRepository
import com.bizsync.sync.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashRepositoryImpl @Inject constructor(
    private val hashStorage: HashStorage
) : HashRepository {

    override suspend fun clearAllHashes() {
        hashStorage.clearAllHashes()
    }


}