package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security
import com.bizsync.domain.constants.enumClass.TipoNota

// Aggiorniamo il modello TipoNotaUi per includere la descrizione
data class TipoNotaUi(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val descrizione: String = ""
)

