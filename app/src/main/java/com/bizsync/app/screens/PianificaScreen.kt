package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.viewmodels.PianificaViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.times
import com.bizsync.domain.constants.enumClass.PianificaScreenManager
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.components.EmptyDayCard
import com.bizsync.ui.components.GiornoHeaderCard
import com.bizsync.ui.components.RiepilogoGiornataCard
import com.bizsync.ui.components.SelectionDataEmptyCard
import com.bizsync.ui.components.getNomeGiorno
import com.bizsync.ui.viewmodels.DipartimentoStatus
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import com.bizsync.ui.components.StatusChip
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.format.DateTimeFormatter



@Composable
fun PianificaScreen() {
    val pianificaVM: PianificaViewModel = hiltViewModel()
    val userViewModel = LocalUserViewModel.current

    val userState by userViewModel.uiState.collectAsState()
    val manager = userState.user.isManager
    val azienda = userState.azienda
    val userId = userState.user.uid

    val pianificaState by pianificaVM.uistate.collectAsState()

    LaunchedEffect(Unit) {
        if (manager) {
            pianificaVM.checkWeeklyPlanningStatus(azienda.idAzienda)
        } else {
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
            // Schermata "Inizia Pubblicazione"
            IniziaPubblicazioneScreen(
                publishableWeek = pianificaState.publishableWeek,
                onStartPlanning = {
                    pianificaVM.createWeeklyPlanning(azienda.idAzienda, userId)
                }
            )
        }

        pianificaState.onBoardingDone == true -> {
            PianificaCore(pianificaVM, manager)
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


// pianifica core ssolo per il manager è un altro per il dipednente
@Composable
fun PianificaCore(
    pianificaVM: PianificaViewModel,
    manager: Boolean
) {
    val scaffoldVM = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current


    val userState by userVM.uiState.collectAsState()
    val dipartimenti = userState.azienda.areeLavoro

    val pianificaState by pianificaVM.uistate.collectAsState()
    val selectionData = pianificaState.selectionData

    val weeklyShift = pianificaState.currentWeeklyShift


    val managerVM: PianificaManagerViewModel = hiltViewModel()

    val managerState by managerVM.uiState.collectAsState()


    val turniSettimanali = managerState.turniSettimanali

//    LaunchedEffect(Unit) {
//
//        managerVM.setTurniSettimanali(weeklyShift?.weekStart)
//    }

    val currentScreen by pianificaVM.currentScreen.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con informazioni pianificazione (solo per manager)
        if (manager) {
//            PlanningHeader(
//                pianificaState = pianificaState,
//                onPublish = { /* pianificaVM.publishWeeklyPlanning(idAzienda) */ },
//                onFinalize = { /* pianificaVM.finalizeWeeklyPlanning(idAzienda) */ },
//                onDelete = { /* pianificaVM.deleteWeeklyPlanning(idAzienda) */ }
//            )
        }

        // Calendario
        Calendar(pianificaVM)

        Spacer(modifier = Modifier.height(8.dp))

        // Contenuto principale
        selectionData?.let { giornoSelezionato ->
            when (currentScreen) {

                // QUI DENTRO DOBBIAMO CONTROLALRE E FARE LA DIFFERENZA TRA GLI SCREEN DEL MANAGER E DEL DIPENDENTE
                PianificaScreenManager.MAIN -> {
                    PianificaGiornata(
                        dipartimenti = dipartimenti,
                        giornoSelezionato = selectionData,
                        managerVM = managerVM,
                        onDipartimentoClick = { pianificaVM.openGestioneTurni(it) },
                        weeklyShift = weeklyShift
                    )
                }

                PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO -> {
                    pianificaState.dipartimento?.let { dip ->
                        GestioneTurniDipartimentoScreen(
                            dipartimento = dip,
                            giornoSelezionato = selectionData,
                            managerVM = managerVM,
                            onBack = { pianificaVM.backToMain() })
                    }
                }

            }
        } ?: run { SelectionDataEmptyCard() }
    }
}
