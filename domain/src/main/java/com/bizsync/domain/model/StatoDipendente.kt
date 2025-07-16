package com.bizsync.domain.model

import java.time.LocalTime

data class StatoDipendente(
    val isAssenteTotale: Boolean = false,
    val assenzaParziale: AssenzaParziale? = null,
    val turnoAssegnato: Boolean = false,
    val note: String? = null
)

