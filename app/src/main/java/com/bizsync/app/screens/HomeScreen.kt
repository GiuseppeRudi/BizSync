package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import androidx.compose.runtime.getValue
import com.bizsync.app.navigation.LocalScaffoldViewModel

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val userVM = LocalUserViewModel.current
    val scaffoldViewModel = LocalScaffoldViewModel.current
    val userState by userVM.uiState.collectAsState()



    LaunchedEffect(Unit) {
        scaffoldViewModel.onFullScreenChanged(false)
    }


}


