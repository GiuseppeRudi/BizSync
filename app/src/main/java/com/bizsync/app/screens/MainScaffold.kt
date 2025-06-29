package com.bizsync.app.screensMore

import androidx.compose.foundation.background
import com.bizsync.app.screens.BottomBar


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val manager = userState.user.isManager
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
                    BottomBar(manager)
                }
            }
        ) { innerPadding ->
            AppNavigator(modifier = Modifier.padding(innerPadding))
        }

    }
}

@Composable
fun ScrollDebugTest() {
    // 1) crea lo ScrollState
    val scrollState = rememberScrollState()

    // 2) mostra sempre in alto l'offset di scroll
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.Red)
        ) {
            // 30 box di test
            repeat(30) { index ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(if (index % 2 == 0) Color.Green else Color.Blue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item #$index", color = Color.White)
                }
            }
        }

        // 3) Debug overlay con l'offset corrente
        Text(
            text = "scroll offset: ${scrollState.value}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        )
    }
}


