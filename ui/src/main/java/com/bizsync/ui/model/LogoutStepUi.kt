package com.bizsync.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class LogoutStepUi(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val color: Color
)