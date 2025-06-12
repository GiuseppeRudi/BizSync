package com.bizsync.domain.model

data class Pausa(
    val id: String = "",
    val nome: String = "",
    val durataminuti: Int = 0,
    val isRetribuita: Boolean = false,
    val isBreak: Boolean = false
) {
    fun isValid(): Boolean {
        return if (isBreak) {
            durataminuti >= 60 // Break deve essere almeno 1 ora
        } else {
            durataminuti > 0
        }
    }
}
