package com.bizsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import com.bizsync.ui.model.BottomNavItem
import com.bizsync.ui.navigation.LocalNavController

@Composable
fun BottomBar(
    manager: Boolean
) {
    val navController = LocalNavController.current

    // Lista dei pulsanti in base al ruolo
    val buttons = if (manager) {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Turni,
            BottomNavItem.Chat,
            BottomNavItem.Gestione,
        )
    } else {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Turni,
            BottomNavItem.Chat,
            BottomNavItem.Gestione
        )
    }


    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            buttons.forEach { item ->
                BottomBarButton(icon = item.icon, label = item.label) {
                    navController.navigate(item.route) {
                        popUpTo("home") { inclusive = false }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarButton(icon: ImageVector, label: String, onClick : () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
