package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizsync.app.screens.BadgeVirtualeScreen
import com.bizsync.app.screens.ChatScreen
import com.bizsync.app.screens.GestioneScreen
import com.bizsync.app.screens.HomeScreen
import com.bizsync.app.screens.MainHomeScreen
import com.bizsync.app.screens.PianificaScreen
import com.bizsync.domain.constants.sealedClass.Screen

@Composable
fun AppNavigator(modifier: Modifier = Modifier, onLogout: () -> Unit) {
    val navController = LocalNavController.current

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier // Importante: il modifier viene applicato al NavHost
    ) {
        composable(Screen.Home.route) {
            // Passa fillMaxSize() perché il padding è già applicato al NavHost
            MainHomeScreen()
        }
        composable(Screen.Turni.route) {
            PianificaScreen()
        }
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.Gestione.route) {
            GestioneScreen(onLogout)
        }
    }
}
