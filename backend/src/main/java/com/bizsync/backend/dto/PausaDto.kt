package com.bizsync.backend.dto



data class PausaDto(
    val id: String = "",
    val durataMinuti: Long = 0,
    val tipo: String = "PAUSA_PRANZO",
    val èRetribuita: Boolean = false,
    val note: String? = null
)
