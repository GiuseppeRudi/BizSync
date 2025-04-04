package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.navigation.NavController

@Composable
fun BottomBar(navController: NavController) {
    BottomAppBar {
        val buttons = listOf(
            "home" to Icons.Filled.Home,
            "pianifica" to Icons.Filled.DateRange,
            "chat" to Icons.Filled.Call,
            "gestione" to Icons.Filled.Build,
            "grafici" to Icons.Filled.Info
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            buttons.forEach { (label, icon) ->
                BottomBarButton(icon, label) {navController.navigate(label)
                { popUpTo("home") {inclusive = false} }}
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
