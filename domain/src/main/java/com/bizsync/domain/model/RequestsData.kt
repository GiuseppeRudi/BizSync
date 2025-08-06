package com.bizsync.domain.model

data class RequestsData(
    val pendingRequests: List<Absence>,
    val historyRequests: List<Absence>,
    val totalRequests: Int
)