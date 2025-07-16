package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.bizsync.domain.model.Pausa
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.MembriSelectionDialog
import com.bizsync.ui.components.MembriSelezionatiSummary
import com.bizsync.ui.components.NoteSection
import com.bizsync.ui.components.PauseManagerDialog
import com.bizsync.ui.components.TitoloTurnoField
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.DayOfWeek

// Estensione per compatibilità con il codice esistente
val Pausa.durataminuti: Long
    get() = durata.toMinutes()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoScreen(
    dipartimento : AreaLavoro,
    giornoSelezionato: LocalDate?,
    onHasUnsavedChanges: (Boolean) -> Unit,
    onBack: () -> Unit,
    managerVM: PianificaManagerViewModel,
    turnoId: String? = null // Per modificare un turno esistente
) {
    val scaffoldVM: ScaffoldViewModel = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val managerState by managerVM.uiState.collectAsState()

    val turnoCorrente = managerState.turnoInModifica
    val isLoading = managerState.loading
    val errorMessage = managerState.errorMessage
    val successMessage = managerState.successMessage
    val disponibilitaMembriTurno = managerState.disponibilitaMembriTurno

    // Inizializza il turno all'avvio
    LaunchedEffect(giornoSelezionato, turnoId) {
        if (turnoId != null) {
            // Carica turno esistente per modifica
            // Qui dovresti implementare la logica per caricare il turno dal database
            // Per ora assumiamo che il turno sia già disponibile
        } else if (giornoSelezionato != null) {
            // Crea nuovo turno
            managerVM.iniziaNuovoTurno(
                giornoSelezionato = giornoSelezionato,
                idAzienda = userState.azienda.idAzienda
            )
        }
    }


    LaunchedEffect(giornoSelezionato) {
        managerVM.setturniDipartimento(giornoSelezionato?.dayOfWeek ?: DayOfWeek.MONDAY, dipartimento.id)
    }

    // Gestione fullscreen
    val fullScreen by scaffoldVM.isFullScreen.collectAsState()
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
    }

    val hasChangeShift = managerState.hasChangeShift

    // Gestione messaggi di successo
    LaunchedEffect(hasChangeShift) {
        if (hasChangeShift ) {
            onHasUnsavedChanges(true)
            onBack() // Torna indietro dopo il salvataggio
        }
    }

    // Snackbar per messaggi
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            managerVM.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (managerState.isNuovoTurno) "Nuovo turno" else "Modifica turno"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (managerState.isModificaTurno) {
                        IconButton(
                            onClick = {
                                managerVM.eliminaTurno(turnoCorrente.id)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Elimina turno",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Campo titolo
            TitoloTurnoField(
                value = turnoCorrente.titolo,
                onValueChange = { managerVM.aggiornaTitolo(it) },
                isError = turnoCorrente.titolo.length > 50,
                errorMessage = if (turnoCorrente.titolo.length > 50)
                    "Il titolo non può superare i 50 caratteri" else ""
            )

            Spacer(Modifier.height(16.dp))



            TimeRangePicker(
                startTime = turnoCorrente.orarioInizio,
                endTime = turnoCorrente.orarioFine,
                onStartTimeSelected = { managerVM.aggiornaOrarioInizio(it) },
                onEndTimeSelected = { managerVM.aggiornaOrarioFine(it) },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { managerVM.setShowPauseDialog(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pause configurate",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${turnoCorrente.pause.size} pause • ${turnoCorrente.pause.sumOf { it.durataminuti }} min totali",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Configura pause"
                    )
                }
            }

            // Dialog per gestire le pause
            if (managerState.showPauseDialog) {
                PauseManagerDialog(managerVm = managerVM)
            }

            Spacer(Modifier.height(16.dp))

            // Selezione dipendenti
            var showMembriDialog by remember { mutableStateOf(false) }

            MembriSelezionatiSummary(
                dipendenti = managerState.dipendenti,
                membriSelezionati = managerVM.getDipendentiSelezionati(),
                orarioInizio = turnoCorrente.orarioInizio,
                orarioFine = turnoCorrente.orarioFine,
                onClick = { showMembriDialog = true }
            )

            MembriSelectionDialog(
                showDialog = showMembriDialog,
                disponibiliMembri = disponibilitaMembriTurno,
                membriSelezionati = turnoCorrente.idDipendenti,
                orarioInizio = turnoCorrente.orarioInizio,
                orarioFine = turnoCorrente.orarioFine,
                onDismiss = { showMembriDialog = false },
                onMembriUpdated = { nuoviIds ->
                    managerVM.aggiornaDipendenti(nuoviIds)
                }
            )



            Spacer(Modifier.height(16.dp))

            // Gestione note
            NoteSection(
                note = turnoCorrente.note,
                onNoteUpdated = { nuoveNote ->
                    managerVM.aggiornaNote(nuoveNote)
                }
            )

            Spacer(Modifier.height(24.dp))

            // Informazioni durata
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Riassunto turno",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Durata: ${managerVM.calcolaDurataTurnoCorrente()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Dipendenti: ${turnoCorrente.idDipendenti.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Pause: ${turnoCorrente.pause.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Pulsanti di azione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pulsante Annulla
                OutlinedButton(
                    onClick = {
                        managerVM.pulisciTurnoInModifica()
                        onBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annulla")
                }

                // Pulsante Salva
                Button(
                    onClick = {
                        managerVM.saveTurno(
                            dipartimentoId = dipartimento.id,
                            giornoSelezionato = giornoSelezionato!!,
                            idAzienda = userState.azienda.idAzienda
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) { Text(if (managerState.isNuovoTurno) "Crea Turno" else "Salva Modifiche") }
            }
        }
    }
}