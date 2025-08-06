package com.bizsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.AppNavigator
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import androidx.compose.runtime.getValue
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(onLogout: () -> Unit) {

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()

    val scaffoldVM: ScaffoldViewModel = hiltViewModel()
    val manager = userState.user.isManager
    val fullScreen by scaffoldVM.isFullScreen.collectAsState()

    CompositionLocalProvider(LocalScaffoldViewModel provides scaffoldVM) {

        Scaffold(
            bottomBar = {
                if (!fullScreen) {
                    BottomBar(manager)
                }
            }
        ) { innerPadding ->
            AppNavigator(modifier = Modifier.padding(innerPadding ),onLogout)
        }

    }
}



