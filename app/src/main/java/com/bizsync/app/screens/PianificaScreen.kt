package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Schedule
import com.bizsync.domain.utils.WeeklyWindowCalculator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person

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
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.components.SelectionDataEmptyCard
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.viewmodels.DettagliGiornalieri
import com.bizsync.ui.viewmodels.PianificaEmployeeViewModel
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun PianificaScreen() {
    val pianificaVM: PianificaViewModel = hiltViewModel()
    val userViewModel = LocalUserViewModel.current
    val pianificaState by pianificaVM.uistate.collectAsState()

    val userState by userViewModel.uiState.collectAsState()
    val manager = userState.user.isManager
    val azienda = userState.azienda
    val userId = userState.user.uid
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

    val employeeVM: PianificaEmployeeViewModel = hiltViewModel()
    val employeeState by employeeVM.uiState.collectAsState()

    // Per ora una struttura base
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }

    val selectionData = pianificaState.selectionData


    val weeklyisIdentical = pianificaState.weeklyisIdentical
    val weeklyShiftRiferimento = pianificaState.weeklyShiftRiferimento
    val weeklyShiftAttuale = pianificaState.weeklyShiftAttuale
    val dipartimento = employeeState.dipartimentoEmployee
    // Inizializza i dati del dipendente
    LaunchedEffect(Unit) {
        val dipartimenti = userState.azienda.areeLavoro
        val dipartimento = dipartimenti.find { it.id == userState.user.dipartimento}
        if(dipartimento != null)
        employeeVM.inizializzaDatiEmployee(userState.user.uid, userState.azienda.idAzienda, dipartimento)
    }

    LaunchedEffect(userState) {
        employeeVM.inizializzaDati(userState.user.toDomain(), userState.contratto)
    }

    LaunchedEffect(weeklyShiftRiferimento, weeklyShiftAttuale) {
        if(weeklyShiftRiferimento != null && weeklyShiftAttuale != null && weeklyShiftAttuale == weeklyShiftRiferimento) {
            pianificaVM.setWeeklyShiftIdentical(true)
        } else {
            pianificaVM.setWeeklyShiftIdentical(false)
        }
    }

    LaunchedEffect(selectionData) {
        pianificaVM.backToMain()

        if(selectionData != null) {
            pianificaVM.getWeeklyShiftCorrente(selectionData)

            // ✅ NUOVO: Imposta i turni giornalieri quando cambia la selezione
            employeeVM.setTurniGiornalieri(selectionData)
        }
    }

    LaunchedEffect(Unit) {
        if(weeklyShiftRiferimento != null) {
            val weekStart = weeklyShiftRiferimento.weekStart
            employeeVM.setTurniSettimanaliDipendente(weekStart, userState.azienda.idAzienda, userState.user.uid)
        }
    }

    LaunchedEffect(weeklyShiftAttuale) {
        if (weeklyShiftAttuale != null && !weeklyisIdentical ) {
            employeeVM.setTurniSettimanaliDipendente(weeklyShiftAttuale.weekStart, userState.azienda.idAzienda, userState.user.uid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

                // Mostra statistiche settimanali e info WeeklyShift se disponibili
                if (employeeState.statisticheSettimanali != null && weeklyShiftAttuale != null) {
                    val statistiche = employeeState.statisticheSettimanali
                    val (weekStart, weekEnd) = WeeklyWindowCalculator.getWeekBounds(weeklyShiftAttuale.weekStart)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prima riga: Ore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ore assegnate: ${statistiche?.oreAssegnate}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Ore contrattuali: ${statistiche?.oreContrattuali}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Seconda riga: Periodo settimana
                    Text(
                        text = "Settimana: ${weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Terza riga: Status e data creazione
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status con colore
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val (statusText, statusColor) = when (weeklyShiftAttuale.status) {
                                WeeklyShiftStatus.PUBLISHED -> "Pubblicata" to Color(0xFF4CAF50)
                                WeeklyShiftStatus.DRAFT -> "Bozza" to Color(0xFFFF9800)
                                WeeklyShiftStatus.NOT_PUBLISHED -> "In lavorazione" to Color(0xFF9E9E9E)
                            }

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = "Creata: ${weeklyShiftAttuale.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yy"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            if (employeeState.loading) {
                // Stato di caricamento
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (employeeState.dettagliGiornalieri != null && employeeState.hasTurniGiornalieri) {
                // Mostra i dettagli del giorno selezionato
                DettagliGiornalieri(
                    dettagli = employeeState.dettagliGiornalieri!!,
                    turni = employeeState.turniGiornalieri,
                    onTurnoClick = { turno ->
                        employeeVM.setShowDialogDettagliTurno(true, turno)
                    }
                )
            } else if (selectionData != null) {
                // Nessun turno per la data selezionata, ma mostra info dipartimento
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Nessun turno assegnato",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Non hai turni assegnati per ${selectionData.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        // Mostra informazioni dipartimento anche quando non ci sono turni
                        if (dipartimento != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Dipartimento: ${dipartimento.nomeArea}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val dayOfWeek = selectionData.dayOfWeek
                                    val orariDipartimento = dipartimento.orariSettimanali[dayOfWeek]

                                    if (orariDipartimento != null) {
                                        Text(
                                            text = "Orari: ${orariDipartimento.first.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${orariDipartimento.second.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        Text(
                                            text = "Dipartimento chiuso",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Stato iniziale - nessuna data selezionata
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

    // Dialog per dettagli turno
    if (employeeState.showDialogDettagliTurno && employeeState.turnoSelezionato != null) {
        DettagliTurnoDialog(
            turno = employeeState.turnoSelezionato!!,
            colleghi = employeeVM.getColleghiByTurno(employeeState.turnoSelezionato!!.id),
            onDismiss = { employeeVM.setShowDialogDettagliTurno(false) }
        )
    }
}

@Composable
fun DettagliGiornalieri(
    dettagli: DettagliGiornalieri,
    turni: List<Turno>,
    onTurnoClick: (Turno) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Header con informazioni giornaliere
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Dettagli ${dettagli.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Informazioni dipartimento
                    if (dettagli.nomeDipartimento != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dipartimento: ${dettagli.nomeDipartimento}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )

                        // Orari dipartimento
                        if (dettagli.orarioAperturaDipartimento != null && dettagli.orarioChiusuraDipartimento != null) {
                            Text(
                                text = "Orari dipartimento: ${dettagli.orarioAperturaDipartimento!!.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${dettagli.orarioChiusuraDipartimento!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ore assegnate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${dettagli.oreTotaliAssegnate}h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = "Ore effettive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${dettagli.oreEffettive.toInt()}h ${((dettagli.oreEffettive % 1) * 60).toInt()}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (dettagli.orarioInizio != null && dettagli.orarioFine != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Orario turni: ${dettagli.orarioInizio!!.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${dettagli.orarioFine!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Lista dei turni
        items(turni) { turno ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTurnoClick(turno) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = turno.titolo.ifEmpty { "Turno ${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${turno.calcolaDurata()}h",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (turno.pause.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pause: ${turno.pause.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Colleghi del giorno
        if (dettagli.colleghi.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Colleghi in turno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        dettagli.colleghi.forEach { collega ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${collega.nome} ${collega.cognome}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DettagliTurnoDialog(
    turno: Turno,
    colleghi: List<User>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = turno.titolo.ifEmpty { "Dettagli Turno" },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Orario: ${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                Text("Durata: ${turno.calcolaDurata()}h")

                if (turno.pause.isNotEmpty()) {
                    Text("Pause: ${turno.pause.size}")
                }

                if (colleghi.isNotEmpty()) {
                    Text("Colleghi:")
                    colleghi.forEach { collega ->
                        Text("• ${collega.nome} ${collega.cognome}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
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
        pianificaVM.backToMain()


        if(selectionData!=null )
        {
            pianificaVM.getWeeklyShiftCorrente(selectionData)
        }
    }

    val managerVM: PianificaManagerViewModel = hiltViewModel()



    LaunchedEffect(Unit) {
        if(weeklyShiftRiferimento != null)
        {
            val weekStart = weeklyShiftRiferimento.weekStart
            managerVM.setTurniSettimanali(weekStart,userState.azienda.idAzienda)
        }
    }

    LaunchedEffect(weeklyShiftAttuale) {
        if (weeklyShiftAttuale != null && !weeklyisIdentical) {
            managerVM.setTurniSettimanali(weeklyShiftAttuale.weekStart,userState.azienda.idAzienda)
        }
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
