package com.bizsync.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizsync.ui.screens.ChatScreen
import com.bizsync.ui.screens.GestioneScreen
import com.bizsync.ui.screens.MainHomeScreen
import com.bizsync.ui.screens.PianificaScreen
import com.bizsync.domain.constants.sealedClass.Screen

@Composable
fun AppNavigator(modifier: Modifier = Modifier, onLogout: () -> Unit) {
    val navController = LocalNavController.current

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
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
