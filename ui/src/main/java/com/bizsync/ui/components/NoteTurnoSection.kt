package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.model.Nota
import com.bizsync.ui.mapper.toUiNota
import java.time.LocalDate
import java.util.UUID


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
                        TipoNota.entries.forEach { tipo ->
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
                        autore = "",
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