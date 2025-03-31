package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.DetailsScreen
import com.bizsync.app.screens.HomeScreen

@Composable
fun AppNavigator(){
    val navController = rememberNavController()

    NavHost(navController= navController, startDestination = "home"){
        composable("home") { HomeScreen(navController) }
        composable("details") { DetailsScreen() }
    }
}
