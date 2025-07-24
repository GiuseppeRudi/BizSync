package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


data class TipoNotaUi(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val descrizione: String = ""
)

