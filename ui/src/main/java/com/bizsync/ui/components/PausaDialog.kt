package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.TipoPausa
import com.bizsync.domain.model.Pausa
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import com.bizsync.ui.viewmodels.ValidationResult
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseManagerDialog(
    managerVm: PianificaManagerViewModel
) {
    val uiState by managerVm.uiState.collectAsState()
    val showDialog = uiState.showPauseDialog
    val pause = uiState.turnoInModifica.pause
    val showAddDialog = uiState.showAddEditPauseDialog
    val editingPausa = uiState.pausaInModifica

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { managerVm.chiudiGestionePause() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Gestione Pause")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    // Header con statistiche
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Pause configurate",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${pause.size}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column {
                                Text(
                                    text = "Tempo totale",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${pause.sumOf { it.durata.toMinutes() }} min",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Lista delle pause
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pause) { pausa ->
                            ModernPausaItem(
                                pausa = pausa,
                                onEdit = { managerVm.caricaPausaPerModifica(it) },
                                onDelete = { managerVm.eliminaPausaDalTurno(pausa.id) }
                            )
                        }

                        if (pause.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Nessuna pausa configurata",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Aggiungi la prima pausa per iniziare",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottone per aggiungere pausa
                    Button(
                        onClick = { managerVm.iniziaNuovaPausa() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aggiungi Pausa")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { managerVm.chiudiGestionePause() }
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { managerVm.chiudiGestionePause() }) {
                    Text("Annulla")
                }
            }
        )
    }

    if (showAddDialog) {
        AddEditPausaDialog(
            viewModel = managerVm
        )
    }
}

@Composable
fun ModernPausaItem(
    pausa: Pausa,
    onEdit: (Pausa) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = getTipoPausaIcon(pausa.tipo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = getTipoPausaDisplayName(pausa.tipo),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${pausa.durata.toMinutes()} minuti",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onEdit(pausa) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifica",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Chips per proprietà
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pausa.èRetribuita) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                "Retribuita",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                if (!pausa.note.isNullOrBlank()) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                "Note",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Note,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Note se presenti
            if (!pausa.note.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = pausa.note!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPausaDialog(
    viewModel: PianificaManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val pausa = uiState.pausaInModifica
    val isModifica = uiState.showAddEditPauseDialog

    var selectedTipo by remember { mutableStateOf(pausa?.tipo ?: TipoPausa.PAUSA_PRANZO) }
    var durata by remember { mutableStateOf(pausa?.durata?.toMinutes()?.toString() ?: "30") }
    var isRetribuita by remember { mutableStateOf(pausa?.èRetribuita ?: false) }
    var note by remember { mutableStateOf(pausa?.note ?: "") }
    var showError by remember { mutableStateOf(false) }

    // Aggiorna gli stati quando cambia la pausa
    LaunchedEffect(pausa) {
        pausa?.let {
            selectedTipo = it.tipo
            durata = it.durata.toMinutes().toString()
            isRetribuita = it.èRetribuita
            note = it.note ?: ""
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.pulisciPausaInModifica() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isModifica) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(if (isModifica) "Modifica Pausa" else "Nuova Pausa")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Selezione tipo di pausa
                    Column {
                        Text(
                            text = "Tipo di pausa",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .selectableGroup()
                            ) {
                                TipoPausa.entries.forEach { tipo ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .selectable(
                                                selected = selectedTipo == tipo,
                                                onClick = {
                                                    selectedTipo = tipo
                                                    viewModel.aggiornaTipoPausa(tipo)
                                                },
                                                role = Role.RadioButton
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedTipo == tipo,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = getTipoPausaIcon(tipo),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (selectedTipo == tipo)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = getTipoPausaDisplayName(tipo),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Durata
                    OutlinedTextField(
                        value = durata,
                        onValueChange = {
                            durata = it
                            showError = false
                            // Aggiorna nel ViewModel se valido
                            val durataInt = it.toIntOrNull()
                            if (durataInt != null && durataInt > 0) {
                                viewModel.aggiornaDurataPausa(Duration.ofMinutes(durataInt.toLong()))
                            }
                        },
                        label = { Text("Durata (minuti)") },
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("Inserire una durata valida (1-480 minuti)") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Checkbox retribuita
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRetribuita)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isRetribuita,
                                onCheckedChange = {
                                    isRetribuita = it
                                    viewModel.aggiornaRetribuitaPausa(it)
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pausa retribuita",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Il tempo viene conteggiato nelle ore lavorate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                            viewModel.aggiornaNotePausa(it)
                        },
                        label = { Text("Note (opzionale)") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (val validationResult = viewModel.validaPausa()) {
                        is ValidationResult.Success -> {
                            viewModel.salvaPausaInTurno()
                        }
                        is ValidationResult.Error -> {
                            showError = true
                        }
                    }
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.pulisciPausaInModifica() }) {
                Text("Annulla")
            }
        }
    )
}

// Helper functions
fun getTipoPausaIcon(tipo: TipoPausa): ImageVector {
    return when (tipo) {
        TipoPausa.PAUSA_PRANZO -> Icons.Default.Restaurant
        TipoPausa.PAUSA_CAFFE -> Icons.Default.Coffee
        TipoPausa.RIPOSO_BREVE -> Icons.Default.Hotel
        TipoPausa.TECNICA -> Icons.Default.Build
        TipoPausa.OBBLIGATORIA -> Icons.Default.Gavel
    }
}

fun getTipoPausaDisplayName(tipo: TipoPausa): String {
    return when (tipo) {
        TipoPausa.PAUSA_PRANZO -> "Pausa Pranzo"
        TipoPausa.PAUSA_CAFFE -> "Pausa Caffè"
        TipoPausa.RIPOSO_BREVE -> "Riposo Breve"
        TipoPausa.TECNICA -> "Pausa Tecnica"
        TipoPausa.OBBLIGATORIA -> "Pausa Obbligatoria"
    }
}