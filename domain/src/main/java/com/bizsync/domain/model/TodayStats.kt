package com.bizsync.domain.model

data class TodayStats(
    val turniTotaliAssegnati: Int = 0,
    val dipartimentiAperti: Int = 0,
    val utentiAttiviOggi: Int = 0,
    val turniCompletati: Int = 0,
    val turniIniziati: Int = 0,
    val turniDaIniziare: Int = 0,
    val dipartimentiDetails: List<DipartimentoInfo> = emptyList()
)