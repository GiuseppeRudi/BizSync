package com.bizsync.domain.repository

interface HashRepository {
    suspend fun clearAllHashes()
}