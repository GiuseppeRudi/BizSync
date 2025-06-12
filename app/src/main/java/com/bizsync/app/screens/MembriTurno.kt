package com.bizsync.app.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bizsync.domain.model.Membro

// Dialog per selezione membri
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembriSelectionDialog(
    showDialog: Boolean,
    tuttiIMembri: List<Membro>,
    membriSelezionati: List<String>, // Lista di ID dei membri selezionati
    onDismiss: () -> Unit,
    onMembriUpdated: (List<String>) -> Unit
) {
    var currentMembriSelezionati by remember { mutableStateOf(membriSelezionati.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tutti") }

    LaunchedEffect(membriSelezionati) {
        currentMembriSelezionati = membriSelezionati.toSet()
    }

    val filteredMembri = remember(tuttiIMembri, searchQuery, selectedFilter) {
        tuttiIMembri.filter { membro ->
            val matchesSearch = membro.nomeCompleto.contains(searchQuery, ignoreCase = true) ||
                    membro.ruolo.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Selezionati" -> currentMembriSelezionati.contains(membro.id)
                "Non Selezionati" -> !currentMembriSelezionati.contains(membro.id)
                else -> true
            }
            matchesSearch && matchesFilter && membro.isAttivo
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            title = {
                Column {
                    Text("Seleziona Membri")
                    Text(
                        "${currentMembriSelezionati.size} di ${tuttiIMembri.size} membri selezionati",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column {
                    // Barra di ricerca
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Cerca membri...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cerca")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Cancella")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtri
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("Tutti", "Selezionati", "Non Selezionati")) { filter ->
                            FilterChip(
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                selected = selectedFilter == filter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Azioni rapide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentMembriSelezionati = tuttiIMembri.filter { it.isAttivo }.map { it.id }.toSet()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Seleziona Tutti")
                        }
                        OutlinedButton(
                            onClick = { currentMembriSelezionati = emptySet() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deseleziona Tutti")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lista membri
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(filteredMembri) { membro ->
                            val isSelezionato = currentMembriSelezionati.contains(membro.id)

                            MembroItemSemplice(
                                membro = membro,
                                isSelezionato = isSelezionato,
                                onToggleSelection = { seleziona ->
                                    currentMembriSelezionati = if (seleziona) {
                                        currentMembriSelezionati + membro.id
                                    } else {
                                        currentMembriSelezionati - membro.id
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMembriUpdated(currentMembriSelezionati.toList())
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
}

// Componente singolo membro semplificato
@Composable
fun MembroItemSemplice(
    membro: Membro,
    isSelezionato: Boolean,
    onToggleSelection: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onToggleSelection(!isSelezionato) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelezionato)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Card(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = membro.iniziali,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informazioni membro
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = membro.nomeCompleto,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = membro.ruolo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Checkbox
            Checkbox(
                checked = isSelezionato,
                onCheckedChange = onToggleSelection
            )
        }
    }
}

// Componente riepilogo membri semplificato
@Composable
fun MembriSelezionatiSummary(
    membriSelezionati: List<Membro>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                        text = "Membri assegnati",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${membriSelezionati.size} membri selezionati",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Gestisci membri"
                )
            }

            if (membriSelezionati.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(membriSelezionati.take(4)) { membro ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Column {
                                    Text(
                                        text = membro.nomeCompleto,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = membro.ruolo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            leadingIcon = {
                                Card(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = membro.iniziali,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (membriSelezionati.size > 4) {
                        item {
                            AssistChip(
                                onClick = { },
                                label = { Text("+${membriSelezionati.size - 4}") }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nessun membro selezionato. Tocca per aggiungere membri al turno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}