package com.bizsync.ui.model

// com.bizsync.ui.model.BottomNavItemUi
import androidx.compose.ui.graphics.vector.ImageVector
import com.bizsync.domain.constants.enumClass.BottomNavType

data class BottomNavItemUi(
    val type: BottomNavType,
    val route: String,
    val label: String,
    val icon: ImageVector
)
