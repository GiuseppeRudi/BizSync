package com.bizsync.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Build



sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Turni : BottomNavItem("turni", "Turni", Icons.Filled.DateRange)
    object Chat : BottomNavItem("chat", "Chat", Icons.Filled.Call)
    object Gestione : BottomNavItem("gestione", "Gestione", Icons.Filled.Build)
}