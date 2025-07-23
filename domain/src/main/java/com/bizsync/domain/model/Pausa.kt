package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.TipoPausa
import kotlinx.serialization.Serializable
import java.time.Duration
import java.util.UUID
import com.bizsync.domain.utils.DurationSerializer

@Serializable
data class Pausa(
    val id: String = UUID.randomUUID().toString(),
    @Serializable(with = DurationSerializer::class)
    val durata: Duration,
    val tipo: TipoPausa = TipoPausa.PAUSA_PRANZO,
    val èRetribuita: Boolean = false,
    val note: String? = null
)
