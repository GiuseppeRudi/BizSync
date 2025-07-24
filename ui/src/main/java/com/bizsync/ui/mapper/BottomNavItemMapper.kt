package com.bizsync.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import com.bizsync.domain.constants.enumClass.BottomNavType
import com.bizsync.ui.model.BottomNavItemUi

fun BottomNavType.toUi(): BottomNavItemUi = when (this) {
    BottomNavType.HOME -> BottomNavItemUi(this, "home", "Home", Icons.Filled.Home)
    BottomNavType.TURNI -> BottomNavItemUi(this, "turni", "Turni", Icons.Filled.DateRange)
    BottomNavType.CHAT -> BottomNavItemUi(this, "chat", "Chat", Icons.Filled.Call)
    BottomNavType.GESTIONE -> BottomNavItemUi(this, "gestione", "Gestione", Icons.Filled.Build)
    BottomNavType.GRAFICI -> BottomNavItemUi(this, "grafici", "Grafici", Icons.Filled.Info)
}
