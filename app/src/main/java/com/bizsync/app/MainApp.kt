package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import kotlinx.coroutines.delay


@Composable
fun MainApp() {
    val navController = rememberNavController()
    val splashScreenViewModel : SplashViewModel = viewModel()

    // Usa un LaunchedEffect per nascondere lo SplashScreen dopo un certo tempo
    LaunchedEffect(Unit) {
        delay(2000) // Mostra lo SplashScreen per 2 secondi
        splashScreenViewModel.hideSplash() // Nascondi lo SplashScreen
    }

    // Mostra lo SplashScreen o lo Scaffold in base allo stato
    if (splashScreenViewModel.isSplashVisible.value) {
        SplashScreen()
    } else {
        AppScaffold(navController) // Mostra lo Scaffold dopo lo SplashScreen
    }
}
