package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.Pausa
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseManagerDialog(
    showDialog: Boolean,
    pause: List<Pausa>,
    onDismiss: () -> Unit,
    onPauseUpdated: (List<Pausa>) -> Unit
) {
    var currentPause by remember { mutableStateOf(listOf<Pausa>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPausa by remember { mutableStateOf<Pausa?>(null) }

    LaunchedEffect(pause) {
        currentPause = pause.toList()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Gestione Pause") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Text(
                        "Pause configurate: ${currentPause.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(currentPause) { pausa ->
                            PausaItem(
                                pausa = pausa,
                                onEdit = { editingPausa = it },
                                onDelete = {
                                    currentPause = currentPause.filter { it.id != pausa.id }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aggiungi Pausa")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPauseUpdated(currentPause)
                        onDismiss()
                    }
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per aggiungere/modificare una pausa
    if (showAddDialog || editingPausa != null) {
        AddEditPausaDialog(
            pausa = editingPausa,
            onDismiss = {
                showAddDialog = false
                editingPausa = null
            },
            onConfirm = { nuovaPausa ->
                if (editingPausa != null) {
                    // Modifica
                    currentPause = currentPause.map {
                        if (it.id == editingPausa!!.id) nuovaPausa else it
                    }
                } else {
                    // Aggiunta
                    val pausaConId = nuovaPausa.copy(id = java.util.UUID.randomUUID().toString())
                    currentPause = currentPause + pausaConId
                }
                showAddDialog = false
                editingPausa = null
            }
        )
    }
}


@Composable
fun PausaItem(
    pausa: Pausa,
    onEdit: (Pausa) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pausa.nome.ifEmpty { "Pausa ${pausa.durataminuti}min" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${pausa.durataminuti} minuti",
                    style = MaterialTheme.typography.bodySmall
                )
                Row {
                    if (pausa.isRetribuita) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Retribuita", style = MaterialTheme.typography.labelSmall) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (pausa.isBreak) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Break", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = { onEdit(pausa) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifica")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPausaDialog(
    pausa: Pausa?,
    onDismiss: () -> Unit,
    onConfirm: (Pausa) -> Unit
) {
    var nome by remember { mutableStateOf(pausa?.nome ?: "") }
    var durata by remember { mutableStateOf(pausa?.durataminuti?.toString() ?: "") }
    var isRetribuita by remember { mutableStateOf(pausa?.isRetribuita ?: false) }
    var isBreak by remember { mutableStateOf(pausa?.isBreak ?: false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pausa != null) "Modifica Pausa" else "Nuova Pausa") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome pausa (opzionale)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = durata,
                    onValueChange = {
                        durata = it
                        showError = false
                    },
                    label = { Text("Durata (minuti)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("${if (isBreak) "Break deve essere almeno 60 minuti" else "Inserire una durata valida"}") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isRetribuita,
                            onCheckedChange = { isRetribuita = it }
                        )
                        Text("Retribuita")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isBreak,
                            onCheckedChange = { isBreak = it }
                        )
                        Text("Break (min 1h)")
                    }
                }

                if (isBreak && durata.toIntOrNull() != null && durata.toInt() < 60) {
                    Text(
                        text = "⚠️ I break devono durare almeno 60 minuti",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val durataInt = durata.toIntOrNull()
                    if (durataInt != null && durataInt > 0) {
                        if (isBreak && durataInt < 60) {
                            showError = true
                        } else {
                            val nuovaPausa = Pausa(
                                id = pausa?.id ?: "",
                                nome = nome,
                                durataminuti = durataInt,
                                isRetribuita = isRetribuita,
                                isBreak = isBreak
                            )
                            onConfirm(nuovaPausa)
                        }
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

