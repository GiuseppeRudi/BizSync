package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.AppNavigator
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import androidx.compose.runtime.getValue
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(onLogout: () -> Unit) {

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()

    val azienda = userState.azienda
    val scaffoldVM: ScaffoldViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val fullScreen by scaffoldVM.fullScreen.collectAsState()

    CompositionLocalProvider(LocalScaffoldViewModel provides scaffoldVM) {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (fullScreen) {
                    TopAppBar(
                        title = {
                            Text(
                                azienda.nome,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scaffoldVM.onFullScreenChanged(false) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onLogout() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                if (fullScreen) {
                    BottomBar()
                }
            }
        ) { innerPadding ->
            AppNavigator(modifier = Modifier.padding(innerPadding))
        }

    }
}



