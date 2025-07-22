package com.bizsync.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.ui.components.TurnoFrequenteDialog


@Composable
fun TurniFrequentiManagementScreen(
    currentTurni: List<TurnoFrequente>,
    onBackClick: () -> Unit,
    onSaveChanges: (List<TurnoFrequente>) -> Unit,
    isLoading: Boolean
) {
    // Stato locale per le modifiche
    var turniModificati by remember { mutableStateOf(currentTurni.toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTurno by remember { mutableStateOf<TurnoFrequente?>(null) }
    var hasChanges by remember { mutableStateOf(false) }

    // Controlla se ci sono modifiche
    LaunchedEffect(turniModificati) {
        hasChanges = turniModificati != currentTurni
    }

    val scaffoldVm = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) { scaffoldVm.onFullScreenChanged(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }

                    Column {
                        Text(
                            text = "Gestione Turni Frequenti",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${turniModificati.size}/6 configurati",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bottone Aggiungi
                    if (turniModificati.size < 6) {
                        IconButton(
                            onClick = { showAddDialog = true },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aggiungi"
                            )
                        }
                    }

                    if (hasChanges) {
                        Button(
                            onClick = { onSaveChanges(turniModificati) },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Salva")
                            }
                        }
                    }
                }
            }
        }

        // Lista dei turni
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = turniModificati,
                key = { it.id }
            ) { turno ->
                TurnoItemEditable(
                    turno = turno,
                    onEdit = {
                        editingTurno = turno
                    },
                    onDelete = {
                        turniModificati = turniModificati.filter { it.id != turno.id }.toMutableList()
                    },
                    enabled = !isLoading
                )
            }

            if (turniModificati.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nessun turno frequente configurato",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog per aggiungere nuovo turno
    if (showAddDialog) {
        TurnoFrequenteDialog(
            turno = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { nome, oraInizio, oraFine ->
                val nuovoTurno = TurnoFrequente(
                    nome = nome,
                    oraInizio = oraInizio,
                    oraFine = oraFine
                )
                turniModificati = (turniModificati + nuovoTurno).toMutableList()
                showAddDialog = false
            }
        )
    }

    // Dialog per modificare turno esistente
    if (editingTurno != null) {
        TurnoFrequenteDialog(
            turno = editingTurno,
            onDismiss = { editingTurno = null },
            onConfirm = { nome, oraInizio, oraFine ->
                turniModificati = turniModificati.map { turno ->
                    if (turno.id == editingTurno!!.id) {
                        turno.copy(nome = nome, oraInizio = oraInizio, oraFine = oraFine)
                    } else {
                        turno
                    }
                }.toMutableList()
                editingTurno = null
            }
        )
    }
}


@Composable
private fun TurnoItemEditable(
    turno: TurnoFrequente,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    Text(
                        text = turno.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${turno.oraInizio} - ${turno.oraFine}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


