package com.bizsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.PianificaViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun PianificaScreen() {
    val pianificaVM: PianificaViewModel = hiltViewModel()
    val userViewModel = LocalUserViewModel.current
    val pianificaState by pianificaVM.uistate.collectAsState()

    val userState by userViewModel.uiState.collectAsState()
    val manager = userState.user.isManager
    val azienda = userState.azienda
    val weeklyPlanningExists = pianificaState.weeklyPlanningExists


    LaunchedEffect(Unit) { pianificaVM.checkWeeklyPlanningStatus(azienda.idAzienda) }

    LaunchedEffect(weeklyPlanningExists) {
        if(weeklyPlanningExists !=null && !manager)
        {
            pianificaVM.setOnBoardingDone(true)
        }
    }

    when {
        pianificaState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        pianificaState.errorMsg != null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pianificaState.errorMsg!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        manager && pianificaState.weeklyPlanningExists == false -> {
            // Schermata "Inizia Pubblicazione" solo per manager
            IniziaPubblicazioneScreen(
                publishableWeek = pianificaState.publishableWeek,
                pianificaViewModel = pianificaVM
            )
        }

        pianificaState.onBoardingDone == true -> {
            // Distingui tra Manager e Dipendenti
            if (manager) {
                PianificaManagerScreen(pianificaVM)
            } else {
                PianificaDipendentiScreen(pianificaVM)
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}





