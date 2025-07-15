package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.viewmodels.PianificaViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.AppLaunchChecker
import com.bizsync.domain.constants.enumClass.PianificaScreenManager
import com.bizsync.ui.components.SelectionDataEmptyCard
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate


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

    val weeklyisIdentical = pianificaState.weeklyisIdentical
    val weeklyShiftRiferimento = pianificaState.weeklyShiftRiferimento
    val weeklyShiftAttuale = pianificaState.weeklyShiftAttuale

    LaunchedEffect(weeklyShiftRiferimento, weeklyShiftAttuale) {
        if(weeklyShiftRiferimento != null && weeklyShiftAttuale != null && weeklyShiftAttuale == weeklyShiftRiferimento)
        {
            pianificaVM.setWeeklyShiftIdentical(true)
        }
        else{
            pianificaVM.setWeeklyShiftIdentical(false)
        }
    }


    LaunchedEffect(selectionData)
    {
        if(selectionData!=null)
        {
            pianificaVM.getWeeklyShiftCorrente(selectionData)
        }
    }

    val managerVM: PianificaManagerViewModel = hiltViewModel()

    val managerState by managerVM.uiState.collectAsState()

    var weekStart = LocalDate.now()

    val turniSettimanali = managerState.turniSettimanali

    LaunchedEffect(Unit) {
        if(weeklyShiftRiferimento != null)
        {
             weekStart = weeklyShiftRiferimento.weekStart

        }

        managerVM.setTurniSettimanali(weekStart)
    }

    LaunchedEffect(selectionData) {
        pianificaVM.backToMain()
    }

    // Carica i dipendenti
    LaunchedEffect(userState.azienda.idAzienda) {
        managerVM.inizializzaDatiDipendenti(userState.azienda.idAzienda)


    }



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
                        weeklyShift = weeklyShiftRiferimento
                    )
                }

                PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO -> {
                    pianificaState.dipartimento?.let { dip ->
                        GestioneTurniDipartimentoScreen(
                            dipartimento = dip,
                            giornoSelezionato = selectionData,
                            weeklyIsIdentical = weeklyisIdentical,
                            onCreateShift = { pianificaVM.openCreateShift() },
                            managerVM = managerVM,
                            onBack = { pianificaVM.backToMain() })
                    }
                }

                PianificaScreenManager.CREATE_SHIFT -> {
                    pianificaState.dipartimento?.let { dip ->
                        TurnoScreen(
                            giornoSelezionato= giornoSelezionato,
                            onBack = { managerVM.setShowDialogCreateShift(false)},
                            managerVM =  managerVM,
                        )
                    }
                }


            }
        } ?: run { SelectionDataEmptyCard() }
    }
}
