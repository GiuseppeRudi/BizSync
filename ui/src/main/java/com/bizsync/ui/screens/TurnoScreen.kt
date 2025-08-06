package com.bizsync.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bizsync.domain.model.Pausa
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.StatoDipendente
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.model.User
import com.bizsync.ui.components.NoteSection
import com.bizsync.ui.components.PauseManagerDialog
import com.bizsync.ui.components.TitoloTurnoField
import com.bizsync.ui.components.TurniFrequentiSelectorCompact
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
) {
    val scaffoldVM: ScaffoldViewModel = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val managerState by managerVM.uiState.collectAsState()

    val turnoCorrente = managerState.turnoInModifica
    val isLoading = managerState.loading
    val errorMessage = managerState.errorMessage
    val disponibilitaMembriTurno = managerState.disponibilitaMembriTurno



    val turniFrequenti = userState.azienda.turniFrequenti

    LaunchedEffect(giornoSelezionato) {
        managerVM.setturniDipartimento(giornoSelezionato?.dayOfWeek ?: DayOfWeek.MONDAY, dipartimento.nomeArea)
    }

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
    }

    val hasChangeShift = managerState.hasChangeShift

    // Gestione messaggi di successo
    LaunchedEffect(hasChangeShift) {
        if (hasChangeShift ) {
            onHasUnsavedChanges(true)
            onBack()
        }
    }

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
                    IconButton(
                        onClick = {
                            managerVM.pulisciTurnoInModifica()
                            onBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (turniFrequenti.isNotEmpty()) {
                        TurniFrequentiSelectorCompact(
                            turniFrequenti = turniFrequenti,
                            onTurnoFrequenteSelezionato = { turno ->
                                managerVM.applicaTurnoFrequente(turno)
                            }
                        )
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
                        "Il titolo non può superare i 50 caratteri" else "",
                )



                // Messaggio informativo se non ci sono turni frequenti
                if (turniFrequenti.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Configura turni frequenti nell'azienda per velocizzare la creazione",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }


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
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Configura pause"
                    )
                }
            }

            if (managerState.showPauseDialog) {
                PauseManagerDialog(managerVm = managerVM)
            }

            Spacer(Modifier.height(16.dp))

            var showMembriDialog by remember { mutableStateOf(false) }

            MembriSelezionatiSummary(
                dipendenti = managerState.dipendenti,
                membriSelezionati = managerVM.getDipendentiSelezionati(),
                zoneLavorativeAssegnate = managerVM.getZoneLavorativeAssegnate(),
                orarioInizio = turnoCorrente.orarioInizio,
                orarioFine = turnoCorrente.orarioFine,
                onClick = { showMembriDialog = true }
            )

            MembriSelectionDialog(
                showDialog = showMembriDialog,
                disponibiliMembri = disponibilitaMembriTurno,
                membriSelezionati = turnoCorrente.idDipendenti,
                zoneLavorativeAssegnate = turnoCorrente.zoneLavorative,
                orarioInizio = turnoCorrente.orarioInizio,
                orarioFine = turnoCorrente.orarioFine,
                onDismiss = { showMembriDialog = false },
                onMembriUpdated = { nuoviIds ->
                    managerVM.aggiornaDipendentiConZone(nuoviIds)
                },
                onZonaLavorativaChanged = { idDipendente, zona ->
                    managerVM.aggiornaZonaLavorativaDipendente(idDipendente, zona)
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

            Spacer(Modifier.height(100.dp))

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
                            dipartimento = dipartimento.nomeArea,
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





@Composable
fun MembriSelezionatiSummary(
    dipendenti: List<User>,
    membriSelezionati: List<User>,
    zoneLavorativeAssegnate: Map<String, ZonaLavorativa> = emptyMap(),
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                    text = "Membri del turno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Gestisci membri"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (membriSelezionati.isEmpty()) {
                Text(
                    text = "Nessun membro selezionato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "${membriSelezionati.size} membri selezionati",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mostra i primi 3 membri con le loro zone lavorative
                val membriDaMostrare = membriSelezionati.take(3)
                membriDaMostrare.forEach { membro ->
                    val zona = zoneLavorativeAssegnate[membro.uid] ?: ZonaLavorativa.IN_SEDE

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${membro.nome} ${membro.cognome}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            color = zona.getChipColor(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    zona.getIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = zona.getChipTextColor()
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = zona.getShortName(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = zona.getChipTextColor()
                                )
                            }
                        }
                    }
                }

                if (membriSelezionati.size > 3) {
                    Text(
                        text = "... e altri ${membriSelezionati.size - 3} membri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZonaLavorativa.getChipColor() = when (this) {
    ZonaLavorativa.IN_SEDE -> MaterialTheme.colorScheme.primaryContainer
    ZonaLavorativa.SMART_WORKING -> MaterialTheme.colorScheme.secondaryContainer
    ZonaLavorativa.TRASFERTA -> MaterialTheme.colorScheme.tertiaryContainer
}

@Composable
private fun ZonaLavorativa.getChipTextColor() = when (this) {
    ZonaLavorativa.IN_SEDE -> MaterialTheme.colorScheme.onPrimaryContainer
    ZonaLavorativa.SMART_WORKING -> MaterialTheme.colorScheme.onSecondaryContainer
    ZonaLavorativa.TRASFERTA -> MaterialTheme.colorScheme.onTertiaryContainer
}

private fun ZonaLavorativa.getShortName(): String = when (this) {
    ZonaLavorativa.IN_SEDE -> "Sede"
    ZonaLavorativa.SMART_WORKING -> "SW"
    ZonaLavorativa.TRASFERTA -> "Trasferta"
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembriSelectionDialog(
    showDialog: Boolean,
    disponibiliMembri: DipendentiGiorno,
    membriSelezionati: List<String>,
    zoneLavorativeAssegnate: Map<String, ZonaLavorativa> = emptyMap(),
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    onDismiss: () -> Unit,
    onMembriUpdated: (List<String>) -> Unit,
    onZonaLavorativaChanged: (String, ZonaLavorativa) -> Unit
) {
    if (!showDialog) return

    Log.d("MembriSelectionDialog", "membri di: $disponibiliMembri")
    Log.d("MembriSelectionDialog", "membri se: $membriSelezionati")

    var dipendentiSelezionatiInterni by remember { mutableStateOf(membriSelezionati.toSet()) }
    var zoneTemporanee by remember { mutableStateOf(zoneLavorativeAssegnate.toMutableMap()) }

    LaunchedEffect(membriSelezionati, zoneLavorativeAssegnate) {
        dipendentiSelezionatiInterni = membriSelezionati.toSet()
        zoneTemporanee = zoneLavorativeAssegnate.toMutableMap()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleziona Membri e Zone",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info orario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        val orarioInizioFormatted = orarioInizio.format(formatter)
                        val orarioFineFormatted = orarioFine.format(formatter)

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Orario turno: $orarioInizioFormatted - $orarioFineFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary selezionati
                if (dipendentiSelezionatiInterni.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${dipendentiSelezionatiInterni.size} membri selezionati",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Lista dipendenti
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(disponibiliMembri.utenti) { dipendente ->
                        val isSelected = dipendente.uid in dipendentiSelezionatiInterni
                        val stato = disponibiliMembri.statoPerUtente[dipendente.uid]
                        val zonaCorrente = zoneTemporanee[dipendente.uid] ?: ZonaLavorativa.IN_SEDE

                        DipendenteSelectionCard(
                            dipendente = dipendente,
                            isSelected = isSelected,
                            zonaLavorativa = zonaCorrente,
                            orarioInizio = orarioInizio,
                            orarioFine = orarioFine,
                            stato = stato,
                            onSelectionChanged = { selected ->
                                dipendentiSelezionatiInterni = if (selected) {
                                    dipendentiSelezionatiInterni + dipendente.uid
                                } else {
                                    dipendentiSelezionatiInterni - dipendente.uid
                                }
                            },
                            onZonaChanged = { nuovaZona ->
                                zoneTemporanee = zoneTemporanee.toMutableMap().apply {
                                    put(dipendente.uid, nuovaZona)
                                }
                            }

                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsanti azione
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = {
                            onMembriUpdated(dipendentiSelezionatiInterni.toList())

                            dipendentiSelezionatiInterni.forEach { idDipendente ->
                                val zona = zoneTemporanee[idDipendente] ?: ZonaLavorativa.IN_SEDE
                                onZonaLavorativaChanged(idDipendente, zona)
                            }

                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Conferma")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DipendenteSelectionCard(
    dipendente: User,
    isSelected: Boolean,
    zonaLavorativa: ZonaLavorativa,
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    stato: StatoDipendente?,
    onSelectionChanged: (Boolean) -> Unit,
    onZonaChanged: (ZonaLavorativa) -> Unit
) {
    var showZonaDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header con checkbox e info dipendente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChanged
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${dipendente.nome} ${dipendente.cognome}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dipendente.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Indicator stato
                DipendenteStatusIndicator(stato, orarioInizio, orarioFine)
            }

            // Sezione zona lavorativa (visibile solo se selezionato)
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Zona lavorativa",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Zona lavorativa:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dropdown per zona lavorativa
                    ExposedDropdownMenuBox(
                        expanded = showZonaDropdown,
                        onExpandedChange = { showZonaDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = zonaLavorativa.getDisplayName(),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showZonaDropdown)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        ExposedDropdownMenu(
                            expanded = showZonaDropdown,
                            onDismissRequest = { showZonaDropdown = false }
                        ) {
                            ZonaLavorativa.entries.forEach { zona ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                zona.getIcon(),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(zona.getDisplayName())
                                        }
                                    },
                                    onClick = {
                                        onZonaChanged(zona)
                                        showZonaDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension functions per ZonaLavorativa
private fun ZonaLavorativa.getDisplayName(): String = when (this) {
    ZonaLavorativa.IN_SEDE -> "In Sede"
    ZonaLavorativa.SMART_WORKING -> "Smart Working"
    ZonaLavorativa.TRASFERTA -> "Trasferta"
}

private fun ZonaLavorativa.getIcon() = when (this) {
    ZonaLavorativa.IN_SEDE -> Icons.Default.Business
    ZonaLavorativa.SMART_WORKING -> Icons.Default.Home
    ZonaLavorativa.TRASFERTA -> Icons.Default.FlightTakeoff
}

@Composable
private fun DipendenteStatusIndicator(
    stato: StatoDipendente?,
    orarioInizio: LocalTime,
    orarioFine: LocalTime
) {
    when {
        stato?.isAssenteTotale == true -> {
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = CircleShape,
                modifier = Modifier.size(8.dp)
            ) {}
        }
        stato?.assenzaParziale != null -> {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
                modifier = Modifier.size(8.dp)
            ) {}
        }
        else -> {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(8.dp)
            ) {}
        }
    }
}


