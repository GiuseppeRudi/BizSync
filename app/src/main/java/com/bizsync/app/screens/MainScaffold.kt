package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.AppNavigator
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import androidx.compose.runtime.getValue
import com.bizsync.app.navigation.LocalUserViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AppScaffold(onLogout: () -> Unit) {

    var userVM = LocalUserViewModel.current

    val azienda by userVM.azienda.collectAsState()
    var scaffoldVM : ScaffoldViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val fullScreen by scaffoldVM.fullScreen.collectAsState()

    if(fullScreen) {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            azienda.Nome,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scaffoldVM.onFullScreenChanged(false)}) {
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
            },
            bottomBar = {
                BottomBar()
            }
        ) { innerPadding ->
            AppNavigator(modifier = Modifier.padding(innerPadding))
        }

    }

    else
    {
        Text(text = "ciao")
    }
}

@Preview(showBackground = true, name = "Scaffold")
@Composable
private fun ScaffoldPreview() {

    fun onLogout()
    {

    }
    MaterialTheme {
        AppScaffold( onLogout = {})
    }
}

