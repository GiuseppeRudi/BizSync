package com.bizsync.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.bizsync.ui.mapper.toUiNota
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.bizsync.domain.model.Pausa
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import java.util.UUID
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.model.User
import com.bizsync.domain.model.Nota
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.ui.components.PauseManagerDialog
import com.bizsync.ui.viewmodels.PianificaManagerViewModel

// Estensione per compatibilità con il codice esistente
val Pausa.durataminuti: Long
    get() = durata.toMinutes()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoScreen(
    giornoSelezionato: LocalDate?,
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


    // Gestione fullscreen
    val fullScreen by scaffoldVM.fullScreen.collectAsState()
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }

    // Gestione messaggi di successo
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
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

    if (fullScreen || isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (turnoCorrente == null) {
        // Stato di errore se non c'è un turno da modificare
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Errore",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Errore nel caricamento del turno",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) {
                    Text("Torna indietro")
                }
            }
        }
    } else {
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

                // Selezione orari
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
                    onClick = { showMembriDialog = true }
                )

                MembriSelectionDialog(
                    showDialog = showMembriDialog,
                    tuttiIMembri = managerState.dipendenti,
                    membriSelezionati = turnoCorrente.idDipendenti,
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
                            managerVM.salvaTurno()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (managerState.isNuovoTurno) "Crea Turno" else "Salva Modifiche")
                        }
                    }
                }
            }
        }
    }
}

// Componente per la selezione dei membri
@Composable
fun MembriSelezionatiSummary(
    dipendenti: List<User>,
    membriSelezionati: List<User>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
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
                    text = "Dipendenti assegnati",
                    style = MaterialTheme.typography.titleMedium
                )
                if (membriSelezionati.isEmpty()) {
                    Text(
                        text = "Nessun dipendente selezionato",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "${membriSelezionati.size} dipendenti selezionati",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Mostra i primi 3 nomi
                    val nomiDaMostrare = membriSelezionati.take(3)
                    val nomiStringa = nomiDaMostrare.joinToString(", ") {
                        if (it.nome.isNotBlank() && it.cognome.isNotBlank()) {
                            "${it.nome} ${it.cognome}"
                        } else {
                            it.email // Fallback all'email se nome e cognome non disponibili
                        }
                    }
                    val altriCount = membriSelezionati.size - nomiDaMostrare.size

                    Text(
                        text = if (altriCount > 0) "$nomiStringa e altri $altriCount" else nomiStringa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Seleziona dipendenti"
            )
        }
    }
}

// Dialog per la selezione dei membri
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembriSelectionDialog(
    showDialog: Boolean,
    tuttiIMembri: List<User>,
    membriSelezionati: List<String>,
    onDismiss: () -> Unit,
    onMembriUpdated: (List<String>) -> Unit
) {
    if (showDialog) {
        var tempSelection by remember { mutableStateOf(membriSelezionati) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Seleziona Dipendenti") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tuttiIMembri) { membro ->
                        val isSelected = tempSelection.contains(membro.uid)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelection = if (isSelected) {
                                        tempSelection - membro.uid
                                    } else {
                                        tempSelection + membro.uid
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (membro.nome.isNotBlank() && membro.cognome.isNotBlank()) {
                                            "${membro.nome} ${membro.cognome}"
                                        } else {
                                            membro.email // Fallback all'email
                                        },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (membro.nome.isNotBlank() && membro.cognome.isNotBlank()) {
                                        Text(
                                            text = membro.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selezionato",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMembriUpdated(tempSelection)
                        onDismiss()
                    }
                ) {
                    Text("Conferma (${tempSelection.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}

// Componente per le note (usando i nuovi modelli di dati)
@Composable
fun NoteSection(
    note: List<Nota>,
    onNoteUpdated: (List<Nota>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var notaInModifica by remember { mutableStateOf<Nota?>(null) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Note del turno",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (note.isEmpty()) "Nessuna nota aggiunta" else "${note.size} note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        notaInModifica = null
                        showNoteDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aggiungi nota"
                    )
                }
            }

            if (note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(note) { nota ->
                        NotaItem(
                            nota = nota,
                            onEdit = {
                                notaInModifica = nota
                                showNoteDialog = true
                            },
                            onDelete = {
                                onNoteUpdated(note.filter { it.id != nota.id })
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog per aggiungere/modificare note
    if (showNoteDialog) {
        NoteDialog(
            nota = notaInModifica,
            onDismiss = {
                showNoteDialog = false
                notaInModifica = null
            },
            onConfirm = { nuovaNota ->
                if (notaInModifica != null) {
                    // Modifica nota esistente
                    onNoteUpdated(note.map { if (it.id == nuovaNota.id) nuovaNota else it })
                } else {
                    // Aggiungi nuova nota
                    onNoteUpdated(note + nuovaNota)
                }
                showNoteDialog = false
                notaInModifica = null
            }
        )
    }
}

// Componente per singola nota
@Composable
fun NotaItem(
    nota: Nota,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tipoNotaUi = nota.tipo.toUiNota()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = tipoNotaUi.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tipoNotaUi.icon,
                        contentDescription = tipoNotaUi.label,
                        tint = tipoNotaUi.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tipoNotaUi.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tipoNotaUi.color
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifica",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nota.testo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Dialog per creare/modificare note
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    nota: Nota?,
    onDismiss: () -> Unit,
    onConfirm: (Nota) -> Unit
) {
    var testo by remember { mutableStateOf(nota?.testo ?: "") }
    var tipoSelezionato by remember { mutableStateOf(nota?.tipo ?: TipoNota.GENERALE) }
    var showTipoDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(if (nota == null) "Nuova Nota" else "Modifica Nota")
        },
        text = {
            Column {
                // Campo testo
                OutlinedTextField(
                    value = testo,
                    onValueChange = { testo = it },
                    label = { Text("Scrivi la tua nota...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 5,
                    supportingText = {
                        Text("${testo.length}/500 caratteri")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selezione tipo
                ExposedDropdownMenuBox(
                    expanded = showTipoDropdown,
                    onExpandedChange = { showTipoDropdown = it }
                ) {
                    val tipoUi = tipoSelezionato.toUiNota()
                    OutlinedTextField(
                        value = tipoUi.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        leadingIcon = {
                            Icon(
                                tipoUi.icon,
                                contentDescription = null,
                                tint = tipoUi.color
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTipoDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showTipoDropdown,
                        onDismissRequest = { showTipoDropdown = false }
                    ) {
                        TipoNota.values().forEach { tipo ->
                            val tipoUi = tipo.toUiNota()
                            DropdownMenuItem(
                                onClick = {
                                    tipoSelezionato = tipo
                                    showTipoDropdown = false
                                },
                                text = { Text(tipoUi.label) },
                                leadingIcon = {
                                    Icon(
                                        tipoUi.icon,
                                        contentDescription = null,
                                        tint = tipoUi.color
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nuovaNota = Nota(
                        id = nota?.id ?: UUID.randomUUID().toString(),
                        testo = testo,
                        tipo = tipoSelezionato,
                        autore = "", // Qui dovresti passare l'ID dell'utente corrente
                        createdAt = nota?.createdAt ?: LocalDate.now(),
                        updatedAt = LocalDate.now()
                    )
                    onConfirm(nuovaNota)
                },
                enabled = testo.isNotBlank()
            ) {
                Text(if (nota == null) "Aggiungi" else "Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

// Componente per il campo titolo turno (già presente nel codice originale)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitoloTurnoField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                showSuggestions = newValue.length <= 2
            },
            label = { Text("Titolo turno") },
            placeholder = { Text("Es. Turno Mattutino - Produzione") },
            leadingIcon = {
                Icon(
                    Icons.Default.Title,
                    contentDescription = "Titolo",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            supportingText = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isError && errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = when {
                                    value.isEmpty() -> "Inserisci un titolo descrittivo"
                                    value.length < 5 -> "Titolo troppo breve"
                                    value.length > 50 -> "Titolo troppo lungo"
                                    else -> "Perfetto! ✓"
                                },
                                color = when {
                                    value.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                                    value.length < 5 || value.length > 50 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "${value.length}/50",
                            color = if (value.length > 50) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            isError = isError || value.length > 50,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        if (value.isNotEmpty() && value.length >= 5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Anteprima",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Anteprima:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}