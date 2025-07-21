package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.ui.viewmodels.HomeViewModel

@Composable
fun MainHomeScreen() {

    val viewModel: HomeViewModel = hiltViewModel()


    val homeState by viewModel.uiState.collectAsState()
    val currentScreen = homeState.currentScreen

    // ViewModel creato UNA SOLA VOLTA
    val userVm = LocalUserViewModel.current
    val userState by userVm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setUserAndAgency(userState.user,userState.azienda)
    }

    when (val screen = currentScreen) {
        HomeScreenRoute.Home -> HomeScreen(viewModel)
        HomeScreenRoute.Badge -> BadgeVirtualeScreen(modifier = Modifier.fillMaxSize(), viewModel)
        HomeScreenRoute.Timbrature -> ManagerTimbratureScreen(viewModel) }
}




@Composable
fun HomeScreen(viewModel : HomeViewModel) {

    val userVm = LocalUserViewModel.current
    val userState by userVm.uiState.collectAsState()

    if (userState.user.isManager) {
        ManagerHomeScreen(userState = userState, modifier = Modifier.fillMaxSize(), viewModel)
    } else {
        EmployeeHomeScreen(userState = userState, modifier = Modifier.fillMaxSize(), viewModel)
    }

}



