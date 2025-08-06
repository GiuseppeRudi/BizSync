package com.bizsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.ui.viewmodels.BadgeViewModel

@Composable
fun MainHomeScreen() {

    val viewModel: BadgeViewModel = hiltViewModel()


    val homeState by viewModel.uiState.collectAsState()
    val currentScreen = homeState.currentScreen



    when (currentScreen) {
        HomeScreenRoute.Home -> HomeScreen(viewModel)
        HomeScreenRoute.Badge -> BadgeVirtualeScreen(modifier = Modifier.fillMaxSize(), viewModel)
    }
}




@Composable
fun HomeScreen(viewModel : BadgeViewModel) {

    val userVm = LocalUserViewModel.current

    val scaffoldVm = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
        scaffoldVm.onFullScreenChanged(false)
    }

    val userState by userVm.uiState.collectAsState()

    if (userState.user.isManager) {
        ManagerHomeScreen(userState = userState,)
    } else {
        EmployeeHomeScreen(userState = userState, modifier = Modifier.fillMaxSize(), onNavigate = {
            viewModel.changeCurrentScreen(
                HomeScreenRoute.Badge
            )
        })
    }

}



