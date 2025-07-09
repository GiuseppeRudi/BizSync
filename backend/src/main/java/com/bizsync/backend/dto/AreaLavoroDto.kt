package com.bizsync.backend.dto

import com.google.firebase.firestore.Exclude

data class AreaLavoroDto(
    @get:Exclude
    val id: String,
    val nomeArea: String,
    val orariSettimanali: Map<String, Map<String, String>>
) {
    constructor() : this(
        id = "",
        nomeArea = "",
        orariSettimanali = emptyMap()
    )
}
