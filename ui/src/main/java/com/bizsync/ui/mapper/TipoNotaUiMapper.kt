package com.bizsync.ui.mapper

import androidx.compose.material.icons.Icons
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.ui.model.TipoNotaUi

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security


/**
 * Mapper per convertire TipoNota del domain in TipoNotaUi per l'interfaccia
 */
fun TipoNota.toUiNota(): TipoNotaUi {
    return when (this) {
        TipoNota.GENERALE -> TipoNotaUi(
            label = "Generale",
            icon = Icons.Default.Note,
            color = Color(0xFF6366F1),
            descrizione = "Nota generica per informazioni standard"
        )
        TipoNota.IMPORTANTE -> TipoNotaUi(
            label = "Importante",
            icon = Icons.Default.PriorityHigh,
            color = Color(0xFFEF4444),
            descrizione = "Informazione prioritaria che richiede attenzione"
        )
        TipoNota.SICUREZZA -> TipoNotaUi(
            label = "Sicurezza",
            icon = Icons.Default.Security,
            color = Color(0xFFEAB308),
            descrizione = "Istruzioni e avvertenze sulla sicurezza"
        )
        TipoNota.CLIENTE -> TipoNotaUi(
            label = "Cliente",
            icon = Icons.Default.Person,
            color = Color(0xFF10B981),
            descrizione = "Informazioni specifiche sui clienti"
        )
        TipoNota.EQUIPMENT -> TipoNotaUi(
            label = "Attrezzature",
            icon = Icons.Default.Build,
            color = Color(0xFF8B5CF6),
            descrizione = "Note su attrezzature e strumentazione"
        )
        TipoNota.PROCEDURA -> TipoNotaUi(
            label = "Procedura",
            icon = Icons.Default.Assignment,
            color = Color(0xFF06B6D4),
            descrizione = "Istruzioni operative e procedure"
        )
    }
}

/**
 * Ottiene tutti i tipi di nota disponibili con la loro rappresentazione UI
 */
fun getAllTipiNotaUi(): List<TipoNotaUi> {
    return TipoNota.values().map { it.toUiNota() }
}

/**
 * Ottiene i tipi di nota più comuni per suggerimenti rapidi
 */
fun getTipiNotaComuni(): List<TipoNotaUi> {
    return listOf(
        TipoNota.GENERALE,
        TipoNota.IMPORTANTE,
        TipoNota.SICUREZZA,
        TipoNota.PROCEDURA
    ).map { it.toUiNota() }
}

/**
 * Ottiene il tipo di nota da una stringa (utile per parsing)
 */
fun String.toTipoNota(): TipoNota? {
    return TipoNota.values().firstOrNull { tipo ->
        tipo.name.equals(this, ignoreCase = true) ||
                tipo.toUiNota().label.equals(this, ignoreCase = true)
    }
}

/**
 * Verifica se un tipo di nota è considerato ad alta priorità
 */
fun TipoNota.isAltaPriorita(): Boolean {
    return this == TipoNota.IMPORTANTE || this == TipoNota.SICUREZZA
}

/**
 * Ottiene la priorità numerica di un tipo di nota (più basso = più prioritario)
 */
fun TipoNota.getPriorita(): Int {
    return when (this) {
        TipoNota.IMPORTANTE -> 1
        TipoNota.SICUREZZA -> 2
        TipoNota.CLIENTE -> 3
        TipoNota.EQUIPMENT -> 4
        TipoNota.PROCEDURA -> 5
        TipoNota.GENERALE -> 6
    }
}