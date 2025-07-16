package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            // Schermata "Inizia Pubblicazione" solo per manager
            IniziaPubblicazioneScreen(
                publishableWeek = pianificaState.publishableWeek,
                onStartPlanning = {
                    pianificaVM.createWeeklyPlanning(azienda.idAzienda, userId)
                }
            )
        }

        pianificaState.onBoardingDone == true -> {
            // Distingui tra Manager e Dipendenti
            if (manager) {
                PianificaManagerCore(pianificaVM)
            } else {
                PianificaDipendentiCore(pianificaVM)
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

@Composable
fun PianificaDipendentiCore(
    pianificaVM: PianificaViewModel
) {
    val scaffoldVM = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current

    val userState by userVM.uiState.collectAsState()
    val pianificaState by pianificaVM.uistate.collectAsState()

    // Per ora una struttura base
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
        // Inizializza dati specifici per dipendenti
//        pianificaVM.loadDipendentiData(userState.user.uid)
    }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header per dipendenti
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "I Miei Turni",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "Visualizza i tuoi turni assegnati",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendario semplificato per dipendenti (solo visualizzazione)
        Calendar(pianificaVM)

        Spacer(modifier = Modifier.height(16.dp))

        // Area principale per i turni del dipendente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Seleziona una data dal calendario",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Potrai visualizzare i tuoi turni assegnati",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PianificaManagerCore(
    pianificaVM: PianificaViewModel,
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

    val hasUnsavedChanges = pianificaState.hasUnsavedChanges
    val isSyncing = pianificaState.isSyncing

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if(currentScreen != PianificaScreenManager.CREATE_SHIFT)
        {
            PlanningHeader(
                weeklyShift = weeklyShiftRiferimento,
                hasUnsavedChanges = hasUnsavedChanges,
                isLoading = isSyncing,
                onSync = {
                    // Sincronizza le modifiche dalla cache a Firebase
                    pianificaVM.syncTurni(weeklyShiftAttuale?.weekStart)
                },
                onStatoSettimana = { nuovoStato ->
                    // Cambia lo stato della settimana
                    // Questo attiverà automaticamente la sincronizzazione per DRAFT e PUBLISHED
                    pianificaVM.changeStatoWeeklyAttuale(nuovoStato)
                }
            )



            // Calendario
            Calendar(pianificaVM)

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contenuto principale
        selectionData?.let { giornoSelezionato ->
            when (currentScreen) {

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
                            weeklyShift = weeklyShiftAttuale,
                            onBack = { pianificaVM.backToMain() })
                    }
                }

                PianificaScreenManager.CREATE_SHIFT -> {
                    pianificaState.dipartimento?.let { dip ->
                        TurnoScreen(
                            dipartimento = dip,
                            giornoSelezionato= giornoSelezionato,
                            onHasUnsavedChanges = { pianificaVM.setHasUnsavedChanges(it) },
                            onBack = { pianificaVM.setDipartimentoScreen(dip)},
                            managerVM =  managerVM,
                        )
                    }
                }


            }
        } ?: run { SelectionDataEmptyCard() }
    }
}
