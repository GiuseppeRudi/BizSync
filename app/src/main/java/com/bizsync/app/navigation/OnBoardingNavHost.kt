package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizsync.domain.constants.sealedClass.OnboardingScreen
import com.bizsync.app.screens.AddAzienda
import com.bizsync.app.screens.AddUtente
import com.bizsync.app.screens.ChooseAzienda
import com.bizsync.app.screens.ChooseInvito

@Composable
fun OnboardingNavHost(navController: NavHostController,onLogout: () -> Unit, onSuccess: () -> Unit,startDestination : String) {
    NavHost(navController, startDestination =  startDestination) {
        composable(OnboardingScreen.AddUtente.route) {
            AddUtente(onChooseAzienda = { navController.navigate(OnboardingScreen.ChooseAzienda.route) }, onLogout)
        }
        composable(OnboardingScreen.ChooseAzienda.route) {
            ChooseAzienda(
                onLogout,
                onCreaAzienda = { navController.navigate(OnboardingScreen.CreaAzienda.route) },
                onVisualizzaInviti = { navController.navigate(OnboardingScreen.ChooseInvito.route) }
            )
        }
        composable(OnboardingScreen.CreaAzienda.route) {
            AddAzienda(
                onLogout,
                onTerminate = { navController.navigate(OnboardingScreen.Terminate.route)
            })
        }
        composable(OnboardingScreen.ChooseInvito.route) {
            ChooseInvito(onTerminate = {
                navController.navigate(OnboardingScreen.Terminate.route)
            })
        }
        composable(OnboardingScreen.Terminate.route) {
            onSuccess()
        }
    }
}
