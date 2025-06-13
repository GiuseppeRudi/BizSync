package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.model.Turno
import com.bizsync.ui.viewmodels.PianificaViewModel
import com.bizsync.ui.viewmodels.TurnoViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import androidx.compose.runtime.getValue
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.viewmodels.UserViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.bizsync.domain.model.Pausa
import com.bizsync.ui.components.AreeLavoroSelector
import com.bizsync.ui.components.PauseManagerDialog
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoScreen(
    giornoSelezionato: LocalDate?,
    onBack: () -> Unit,
    pianificaVM: PianificaViewModel,
    userVM: UserViewModel,
    scaffoldVM : ScaffoldViewModel
) {
    val turnoVM: TurnoViewModel = hiltViewModel()
    val userState by userVM.uiState.collectAsState()
    val azienda = userState.azienda
    val text by turnoVM.text.collectAsState()
    var note by remember { mutableStateOf(listOf<NotaTurno>()) }


    var membriSelezionatiIds by remember { mutableStateOf(listOf<String>()) }
    var showMembriDialog by remember { mutableStateOf(false) }

    // Assumendo che tu abbia accesso ai membri dell'azienda
    val membriTeam = turnoVM.membriDiProva // o da dove li recuperi

    // Membri selezionati completi
    val membriSelezionati = remember(membriSelezionatiIds, membriTeam) {
        membriTeam.filter { it.id in membriSelezionatiIds }
    }


    var startHour by remember { mutableStateOf("") }
    var endHour by remember { mutableStateOf("") }
    var numPause by remember { mutableStateOf(0) }
    var membri by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf<AreaLavoro?>(null) }

    val fullScreen by scaffoldVM.fullScreen.collectAsState()
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }



    if (fullScreen)
    {
        CircularProgressIndicator()
    }

    else
    {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nuovo turno") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                var titoloTurno by remember { mutableStateOf("") }


                    // Sostituisci il vecchio OutlinedTextField con:
                TitoloTurnoField(
                    value = titoloTurno,
                    onValueChange = { titoloTurno = it },
                    isError = titoloTurno.length > 50,
                    errorMessage = if (titoloTurno.length > 50) "Il titolo non può superare i 50 caratteri" else ""
                )

                Spacer(Modifier.height(8.dp))

                AreeLavoroSelector(
                    selectedArea = selectedArea,
                    areas = azienda.areeLavoro,
                    onAreaSelected = { area -> selectedArea = area },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TimeRangePicker(
                    startTime = startHour,
                    endTime = endHour,
                    onStartTimeSelected = { startHour = it },
                    onEndTimeSelected = { endHour = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(15.dp))

// Sostituisci la variabile numPause con:
                var pause by remember { mutableStateOf(listOf<Pausa>()) }
                var showPauseDialog by remember { mutableStateOf(false) }

// Sostituisci il campo OutlinedTextField delle pause con:
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showPauseDialog = true }
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
                                text = "${pause.size} pause • ${pause.sumOf { it.durataminuti }} min totali",
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

                PauseManagerDialog(
                    showDialog = showPauseDialog,
                    pause = pause,
                    onDismiss = { showPauseDialog = false },
                    onPauseUpdated = { nuovePause -> pause = nuovePause }
                )


                Spacer(Modifier.height(8.dp))


                // Sostituisci il vecchio OutlinedTextField con:
                MembriSelezionatiSummary(
                    membriSelezionati = membriSelezionati,
                    onClick = { showMembriDialog = true }
                )

                // Dialog per selezione membri
                MembriSelectionDialog(
                    showDialog = showMembriDialog,
                    tuttiIMembri = membriTeam,
                    membriSelezionati = membriSelezionatiIds,
                    onDismiss = { showMembriDialog = false },
                    onMembriUpdated = { nuoviIds -> membriSelezionatiIds = nuoviIds }
                )

                Spacer(Modifier.height(8.dp))

                var note by remember { mutableStateOf(listOf<NotaTurno>()) }

                // Sostituisci il vecchio OutlinedTextField con:
                NoteSection(
                    note = note,
                    onNoteUpdated = { nuoveNote -> note = nuoveNote }
                )

                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = {
                        if (text.isNotEmpty() && giornoSelezionato != null) {
                            val timestamp = localDateToTimestamp(giornoSelezionato)
                            turnoVM.aggiungiturno(
                                pianificaVM,
                                Turno(
                                    idDocumento = "",
                                    nome = text,
                                    giorno = timestamp,
                                    // Altri campi qui quando li abiliti
                                )
                            )
                            turnoVM.onTextChanged("")
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aggiungi Turno")
                }
            }
        }
    }

}



fun localDateToTimestamp(localDate: LocalDate): Timestamp {

    val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()) // mezzanotte nel fuso orario locale
    val date = Date.from(startOfDay.toInstant())
    return Timestamp(date)
}




// Data class per le note
data class NotaTurno(
    val id: String = UUID.randomUUID().toString(),
    val testo: String = "",
    val tipo: TipoNota = TipoNota.GENERALE,
    val priorita: PrioritaNota = PrioritaNota.NORMALE,
    val timestamp: Long = System.currentTimeMillis(),
    val autore: String = "" // ID dell'utente che ha creato la nota
)

enum class TipoNota(val label: String, val icon: ImageVector, val color: Color) {
    GENERALE("Generale", Icons.Default.Note, Color(0xFF6366F1)),
    IMPORTANTE("Importante", Icons.Default.PriorityHigh, Color(0xFFEF4444)),
    SICUREZZA("Sicurezza", Icons.Default.Security, Color(0xFFEAB308)),
    CLIENTE("Cliente", Icons.Default.Person, Color(0xFF10B981)),
    EQUIPMENT("Attrezzature", Icons.Default.Build, Color(0xFF8B5CF6)),
    PROCEDURA("Procedura", Icons.Default.Assignment, Color(0xFF06B6D4))
}

enum class PrioritaNota(val label: String, val color: Color) {
    BASSA("Bassa", Color(0xFF10B981)),
    NORMALE("Normale", Color(0xFF6B7280)),
    ALTA("Alta", Color(0xFFEF4444))
}

// Componente principale per le note
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSection(
    note: List<NotaTurno>,
    onNoteUpdated: (List<NotaTurno>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var notaInModifica by remember { mutableStateOf<NotaTurno?>(null) }

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
                    items(note.sortedByDescending { it.timestamp }) { nota ->
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

            // Template note rapide
            if (note.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Template rapidi:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getTemplateNote()) { template ->
                        SuggestionChip(
                            onClick = {
                                val nuovaNota = NotaTurno(
                                    testo = template.testo,
                                    tipo = template.tipo,
                                    priorita = template.priorita
                                )
                                onNoteUpdated(note + nuovaNota)
                            },
                            label = { Text(template.testo.take(20) + "...") },
                            icon = {
                                Icon(
                                    template.tipo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
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
    nota: NotaTurno,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = nota.tipo.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            when (nota.priorita) {
                PrioritaNota.ALTA -> nota.priorita.color
                else -> Color.Transparent
            }
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
                        nota.tipo.icon,
                        contentDescription = nota.tipo.label,
                        tint = nota.tipo.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = nota.tipo.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = nota.tipo.color
                    )

                    if (nota.priorita != PrioritaNota.NORMALE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = nota.priorita.color.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = nota.priorita.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = nota.priorita.color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(nota.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Dialog per creare/modificare note
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    nota: NotaTurno?,
    onDismiss: () -> Unit,
    onConfirm: (NotaTurno) -> Unit
) {
    var testo by remember { mutableStateOf(nota?.testo ?: "") }
    var tipoSelezionato by remember { mutableStateOf(nota?.tipo ?: TipoNota.GENERALE) }
    var prioritaSelezionata by remember { mutableStateOf(nota?.priorita ?: PrioritaNota.NORMALE) }
    var showTipoDropdown by remember { mutableStateOf(false) }
    var showPrioritaDropdown by remember { mutableStateOf(false) }

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
                    OutlinedTextField(
                        value = tipoSelezionato.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        leadingIcon = {
                            Icon(
                                tipoSelezionato.icon,
                                contentDescription = null,
                                tint = tipoSelezionato.color
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
                            DropdownMenuItem(
                                onClick = {
                                    tipoSelezionato = tipo
                                    showTipoDropdown = false
                                },
                                text = { Text(tipo.label) },
                                leadingIcon = {
                                    Icon(
                                        tipo.icon,
                                        contentDescription = null,
                                        tint = tipo.color
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selezione priorità
                Text(
                    text = "Priorità",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrioritaNota.values().forEach { priorita ->
                        FilterChip(
                            onClick = { prioritaSelezionata = priorita },
                            label = { Text(priorita.label) },
                            selected = prioritaSelezionata == priorita,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priorita.color.copy(alpha = 0.2f),
                                selectedLabelColor = priorita.color
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nuovaNota = NotaTurno(
                        id = nota?.id ?: UUID.randomUUID().toString(),
                        testo = testo,
                        tipo = tipoSelezionato,
                        priorita = prioritaSelezionata,
                        timestamp = nota?.timestamp ?: System.currentTimeMillis()
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

// Template note predefinite
fun getTemplateNote(): List<NotaTurno> {
    return listOf(
        NotaTurno(
            testo = "Verificare funzionamento di tutte le attrezzature prima dell'inizio del turno",
            tipo = TipoNota.EQUIPMENT,
            priorita = PrioritaNota.ALTA
        ),
        NotaTurno(
            testo = "Rispettare rigorosamente le procedure di sicurezza",
            tipo = TipoNota.SICUREZZA,
            priorita = PrioritaNota.ALTA
        ),
        NotaTurno(
            testo = "Cliente importante in visita - prestare particolare attenzione",
            tipo = TipoNota.CLIENTE,
            priorita = PrioritaNota.NORMALE
        ),
        NotaTurno(
            testo = "Seguire la checklist standard per le operazioni di routine",
            tipo = TipoNota.PROCEDURA,
            priorita = PrioritaNota.NORMALE
        ),
        NotaTurno(
            testo = "Aggiornamento importante: nuove procedure operative",
            tipo = TipoNota.IMPORTANTE,
            priorita = PrioritaNota.ALTA
        )
    )
}



// Componente principale per il titolo del turno
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
    var selectedTemplate by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        // Campo principale
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // Mostra suggerimenti se il campo è vuoto o ha pochi caratteri
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
            trailingIcon = {
                Row {
                    // Icona AI per suggerimenti intelligenti
                    if (value.length > 5) {
                        IconButton(
                            onClick = {
                                val suggerimento = generaSuggerimentoIntelligente(value)
                                onValueChange(suggerimento)
                            }
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Suggerimento AI",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Pulsante template
                    IconButton(onClick = { showSuggestions = !showSuggestions }) {
                        Icon(
                            if (showSuggestions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Mostra template"
                        )
                    }

                    // Clear button
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Cancella",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            supportingText = {
                Column {
                    // Contatore caratteri con validazione
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

        // Sezione suggerimenti e template
        AnimatedVisibility(
            visible = showSuggestions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Template Rapidi",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Template categorizzati
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(getTemplatesTitoli()) { categoria ->
                            TemplateCategory(
                                categoria = categoria,
                                onTemplateSelected = { template ->
                                    onValueChange(template)
                                    selectedTemplate = template
                                    showSuggestions = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggerimenti intelligenti basati su ora/data
                    Text(
                        text = "Suggerimenti Intelligenti",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getSuggerimentiIntelligenti()) { suggerimento ->
                            SuggestionChip(
                                onClick = {
                                    onValueChange(suggerimento.titolo)
                                    showSuggestions = false
                                },
                                label = { Text(suggerimento.titolo) },
                                icon = {
                                    Icon(
                                        suggerimento.icona,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Anteprima del titolo formattato
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

// Data class per i template
data class TemplateCategoria(
    val nome: String,
    val icona: ImageVector,
    val templates: List<String>,
    val colore: Color
)

data class SuggerimentoIntelligente(
    val titolo: String,
    val icona: ImageVector,
    val descrizione: String
)

// Componente per categoria di template
@Composable
fun TemplateCategory(
    categoria: TemplateCategoria,
    onTemplateSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Header della categoria
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            colors = CardDefaults.cardColors(
                containerColor = categoria.colore.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        categoria.icona,
                        contentDescription = categoria.nome,
                        tint = categoria.colore,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoria.nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = categoria.colore
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Nascondi" else "Mostra"
                )
            }
        }

        // Lista template
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            ) {
                categoria.templates.forEach { template ->
                    TextButton(
                        onClick = { onTemplateSelected(template) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = template,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funzioni helper per i template
fun getTemplatesTitoli(): List<TemplateCategoria> {
    return listOf(
        TemplateCategoria(
            nome = "Turni Standard",
            icona = Icons.Default.Schedule,
            colore = Color(0xFF2563EB),
            templates = listOf(
                "Turno Mattutino - Produzione",
                "Turno Pomeridiano - Assemblaggio",
                "Turno Serale - Controllo Qualità",
                "Turno Notturno - Manutenzione",
                "Turno Weekend - Supervisione"
            )
        ),
        TemplateCategoria(
            nome = "Settori Operativi",
            icona = Icons.Default.Business,
            colore = Color(0xFF059669),
            templates = listOf(
                "Reparto Logistica - Spedizioni",
                "Area Magazzino - Inventario",
                "Linea Produzione A - Setup",
                "Controllo Sicurezza - Ispezione",
                "Servizio Clienti - Supporto"
            )
        ),
        TemplateCategoria(
            nome = "Eventi Speciali",
            icona = Icons.Default.Event,
            colore = Color(0xFFDC2626),
            templates = listOf(
                "Emergenza - Turno Straordinario",
                "Formazione - Nuovo Personale",
                "Manutenzione Programmata",
                "Audit Qualità - Certificazione",
                "Chiusura Mensile - Bilanci"
            )
        ),
        TemplateCategoria(
            nome = "Progetti Speciali",
            icona = Icons.Default.Assignment,
            colore = Color(0xFF7C3AED),
            templates = listOf(
                "Progetto Alpha - Fase Testing",
                "Implementazione Sistema ERP",
                "Training Sicurezza - Livello 2",
                "Upgrading Attrezzature",
                "Revisione Processi - Q1"
            )
        )
    )
}

fun getSuggerimentiIntelligenti(): List<SuggerimentoIntelligente> {
    val ora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val giorno = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    return when {
        ora in 6..11 -> listOf(
            SuggerimentoIntelligente("Turno Mattutino - Apertura", Icons.Default.WbSunny, "Turno di apertura"),
            SuggerimentoIntelligente("Setup Giornaliero", Icons.Default.Settings, "Preparazione giornaliera"),
            SuggerimentoIntelligente("Brief Mattutino", Icons.Default.Groups, "Riunione del mattino")
        )
        ora in 12..17 -> listOf(
            SuggerimentoIntelligente("Turno Pomeridiano", Icons.Default.LightMode, "Turno centrale"),
            SuggerimentoIntelligente("Produzione Intensiva", Icons.Default.Speed, "Picco produttivo"),
            SuggerimentoIntelligente("Controlli Intermedi", Icons.Default.Checklist, "Verifiche pomeridiane")
        )
        ora in 18..23 -> listOf(
            SuggerimentoIntelligente("Turno Serale", Icons.Default.Lock, "Turno di chiusura"),
            SuggerimentoIntelligente("Chiusura Giornaliera", Icons.Default.Lock, "Fine giornata"),
            SuggerimentoIntelligente("Pulizie Serali", Icons.Default.CleaningServices, "Sanificazione")
        )
        else -> listOf(
            SuggerimentoIntelligente("Turno Notturno", Icons.Default.DarkMode, "Supervisione notturna"),
            SuggerimentoIntelligente("Manutenzione Notturna", Icons.Default.Build, "Lavori notturni"),
            SuggerimentoIntelligente("Sicurezza 24h", Icons.Default.Security, "Vigilanza continua")
        )
    }.let { base ->
        if (giorno == Calendar.SATURDAY || giorno == Calendar.SUNDAY) {
            base + SuggerimentoIntelligente("Turno Weekend", Icons.Default.Weekend, "Servizio festivo")
        } else base
    }
}

// Funzione per generare suggerimenti intelligenti basati sul testo
fun generaSuggerimentoIntelligente(testoCorrente: String): String {
    val paroleChiave = mapOf(
        "produzione" to "Turno Produzione - ${getCurrentShift()}",
        "manutenzione" to "Manutenzione Programmata - ${getCurrentDate()}",
        "sicurezza" to "Controllo Sicurezza - Ispezione ${getCurrentTime()}",
        "emergenza" to "Turno Straordinario - Emergenza ${getCurrentTime()}",
        "formazione" to "Sessione Formativa - ${getCurrentDate()}",
        "qualità" to "Controllo Qualità - Verifica ${getCurrentShift()}"
    )

    val parolaChiaveTrovata = paroleChiave.keys.find {
        testoCorrente.lowercase().contains(it)
    }

    return parolaChiaveTrovata?.let { paroleChiave[it] }
        ?: "Turno ${getCurrentShift()} - ${testoCorrente.trim()}"
}

// Helper functions
fun getCurrentShift(): String {
    val ora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (ora) {
        in 6..13 -> "Mattutino"
        in 14..21 -> "Pomeridiano"
        else -> "Notturno"
    }
}

fun getCurrentDate(): String {
    return SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
}

fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

