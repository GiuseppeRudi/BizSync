package com.bizsync.domain.model

data class DipendentiGiorno(
    val utenti: List<User> = emptyList(),
    val statoPerUtente: Map<String, StatoDipendente>  = emptyMap()
)