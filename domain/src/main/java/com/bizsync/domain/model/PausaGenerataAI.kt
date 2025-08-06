package com.bizsync.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class PausaGenerataAI(
    val tipo: String,
    val durataMinuti: Int,
    val retribuita: Boolean
)