package com.bizsync.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ccnlnfo(
    val settore: String = "",
    val ruolo: String = "",
    val ferieAnnue: Int = 0,
    val rolAnnui: Int = 0,
    val stipendioAnnualeLordo: Int = 0,
    val malattiaRetribuita: Int = 0
)