package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.ui.viewmodels.HomeViewModel

@Composable
fun MainHomeScreen() {

    val viewModel: HomeViewModel = hiltViewModel()


    val homeState by viewModel.uiState.collectAsState()
    val currentScreen = homeState.currentScreen



    when (currentScreen) {
        HomeScreenRoute.Home -> HomeScreen(viewModel)
        HomeScreenRoute.Badge -> BadgeVirtualeScreen(modifier = Modifier.fillMaxSize(), viewModel) }
}




@Composable
fun HomeScreen(viewModel : HomeViewModel) {

    val userVm = LocalUserViewModel.current

    val scaffoldVm = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
        scaffoldVm.onFullScreenChanged(false)
    }

    val userState by userVm.uiState.collectAsState()

    if (userState.user.isManager) {
        ManagerHomeScreen(userState = userState,)
    } else {
        EmployeeHomeScreen(userState = userState, modifier = Modifier.fillMaxSize(), onNavigate = {viewModel.changeCurrentScreen(
            HomeScreenRoute.Badge)})
    }

}



