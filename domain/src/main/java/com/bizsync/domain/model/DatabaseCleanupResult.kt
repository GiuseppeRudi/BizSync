package com.bizsync.domain.model

data class DatabaseCleanupResult(
    val cleanupResults: Map<String, Boolean>,
    val totalTables: Int,
    val successfulCleanups: Int,
    val isFullySuccessful: Boolean
)
