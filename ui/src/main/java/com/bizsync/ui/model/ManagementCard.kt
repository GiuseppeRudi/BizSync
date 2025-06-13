package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ManagementCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val onClick: () -> Unit
)
