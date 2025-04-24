package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizsync.app.MainApp
import com.bizsync.app.screens.ChatScreen
import com.bizsync.app.screens.GestioneScreen
import com.bizsync.app.screens.GraficiScreen
import com.bizsync.app.screens.HomeScreen
import com.bizsync.app.screens.PianificaScreen
import com.bizsync.app.screens.WelcomeScreen


@Composable
fun AppNavigator(navController: NavHostController, modifier: Modifier) {

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(navController = navController, startDestination = "home", modifier= modifier) {
            composable("home") { HomeScreen() }
            composable("pianifica") { PianificaScreen() }
            composable("chat") { ChatScreen() }
            composable("gestione") { GestioneScreen() }
            composable("grafici") { GraficiScreen() }
            composable("welcome") { WelcomeScreen() }

        }
    }

}
