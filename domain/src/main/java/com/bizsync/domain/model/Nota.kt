package com.bizsync.domain.model



import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID


@Serializable
data class Nota(
    val id: String = UUID.randomUUID().toString(),
    val testo: String = "",
    val tipo: TipoNota = TipoNota.GENERALE,
    val autore: String = "", // ID dell'utente che ha creato la nota
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate = LocalDate.now(),
    @Serializable(with = LocalDateSerializer::class)
    val updatedAt: LocalDate = LocalDate.now()
) {
    /**
     * Verifica se la nota è stata modificata
     */
    fun isModificata(): Boolean {
        return updatedAt.isAfter(createdAt)
    }

    /**
     * Verifica se la nota è vuota
     */
    fun isEmpty(): Boolean {
        return testo.isBlank()
    }

    /**
     * Ottiene una versione abbreviata del testo
     */
    fun getTestoAbbreviato(maxLength: Int = 50): String {
        return if (testo.length <= maxLength) {
            testo
        } else {
            testo.take(maxLength - 3) + "..."
        }
    }

    /**
     * Verifica se la nota è di tipo importante
     */
    fun isImportante(): Boolean {
        return tipo == TipoNota.IMPORTANTE || tipo == TipoNota.SICUREZZA
    }

    /**
     * Valida se la nota è completa
     */
    fun isValid(): Boolean {
        return testo.isNotBlank() && testo.length <= 500
    }
}

