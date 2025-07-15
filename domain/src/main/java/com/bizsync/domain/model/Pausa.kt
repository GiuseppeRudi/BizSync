package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.TipoPausa
import java.time.Duration
import java.util.UUID

data class Pausa(
    val id: String = UUID.randomUUID().toString(),
    val durata: Duration,
    val tipo: TipoPausa = TipoPausa.PAUSA_PRANZO,
    val èRetribuita: Boolean = false,
    val note: String? = null
)
