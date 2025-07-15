package com.bizsync.domain.model

data class DipendentiGiorno(
    val utenti: List<User>,
    val statoPerUtente: Map<String, StatoDipendente>)