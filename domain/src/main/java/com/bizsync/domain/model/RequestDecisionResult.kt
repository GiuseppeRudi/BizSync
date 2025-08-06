package com.bizsync.domain.model

data class RequestDecisionResult(
    val updatedRequest: Absence,
    val updatedContract: Contratto?,
    val contractUpdateSuccess: Boolean,
    val contractError: String?,
    val isApproved: Boolean
)
