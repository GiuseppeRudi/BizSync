package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizsync.app.screens.ChatScreen
import com.bizsync.app.screens.GestioneScreen
import com.bizsync.app.screens.GraficiScreen
import com.bizsync.app.screens.HomeScreen
import com.bizsync.app.screens.PianificaScreen


@Composable
fun AppNavigator( modifier: Modifier) {

    val navController = LocalNavController.current
    NavHost(navController = navController, startDestination = "home", modifier= modifier) {
        composable("home") { HomeScreen() }
        composable("pianifica") { PianificaScreen() }
        composable("chat") { ChatScreen() }
        composable("gestione") { GestioneScreen() }
        composable("grafici") { GraficiScreen() }
    }

}
