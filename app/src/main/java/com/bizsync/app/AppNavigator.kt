package com.bizsync.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.ChatScreen
import com.bizsync.app.screens.GestioneScreen
import com.bizsync.app.screens.GraficiScreen
import com.bizsync.app.screens.HomeScreen
import com.bizsync.app.screens.PianificaScreen
import com.bizsync.app.screens.BottomBar
import com.bizsync.app.screens.SplashScreen

@Composable
fun AppNavigator(navController: NavHostController, modifier: Modifier) {

    NavHost(navController = navController, startDestination = "home", modifier= modifier) {
        composable("home") { HomeScreen() }
        composable("pianifica") { PianificaScreen() }
        composable("chat") { ChatScreen() }
        composable("gestione") { GestioneScreen() }
        composable("grafici") { GraficiScreen() }
    }

}
