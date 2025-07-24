package com.bizsync.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Note
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.ui.model.TipoNotaUi

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security


fun TipoNota.toUiNota(): TipoNotaUi {
    return when (this) {
        TipoNota.GENERALE -> TipoNotaUi(
            label = "Generale",
            icon = Icons.AutoMirrored.Filled.Note,
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
            icon = Icons.AutoMirrored.Filled.Assignment,
            color = Color(0xFF06B6D4),
            descrizione = "Istruzioni operative e procedure"
        )
    }
}
