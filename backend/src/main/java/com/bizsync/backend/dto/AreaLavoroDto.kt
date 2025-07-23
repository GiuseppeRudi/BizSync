package com.bizsync.backend.dto

import com.google.firebase.firestore.Exclude

data class AreaLavoroDto(
    val nomeArea: String,
    val orariSettimanali: Map<String, Map<String, String>>
) {
    constructor() : this(
        nomeArea = "",
        orariSettimanali = emptyMap()
    )
}
