package com.bizsync.app.screens

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.AreaLavoroDialog
import com.bizsync.ui.viewmodels.CompanyViewModel

@Composable
fun DipartimentiManagementScreen(companyVm: CompanyViewModel, areeLavoro: List<AreaLavoro>, idAzienda : String) {

    val uiState by companyVm.uiState.collectAsState()

    val scaffoldVM = LocalScaffoldViewModel.current
    val areeModificate = uiState.areeModificate
    val showAddDialog = uiState.showAddDialog
    val editingArea = uiState.editingArea
    val hasChanges = uiState.hasChanges
    val isLoading = uiState.isLoading

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
        companyVm.setAreeModificate(areeLavoro)
    }



    LaunchedEffect(areeModificate) {
        val hasDiff = uiState.areeModificate != areeLavoro
        companyVm.setHasChanges(hasDiff)
    }

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
                    IconButton(onClick = {companyVm.setSelectedOperation(null)}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }

                    Column {
                        Text(
                            text = "Gestione Dipartimenti",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${areeModificate.size}/10 configurati",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bottone Aggiungi
                    if (areeModificate.size < 10) {
                        IconButton(
                            onClick = { companyVm.setShowAddDialog(true) },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aggiungi"
                            )
                        }
                    }

                    // Bottone Salva (solo se ci sono modifiche)
                    if (hasChanges) {
                        Button(
                            onClick = { companyVm.onSaveChanges(idAzienda) },
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

        // Lista delle aree
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = areeModificate,
                key = { it.id }
            ) { area ->
                AreaItemEditable(
                    area = area,
                    onEdit = {
                        companyVm.setEditingArea(area)
                    },
                    onDelete = {
                        companyVm.removeAreaModificata(area)
                    },
                    enabled = !isLoading
                )
            }

            if (areeModificate.isEmpty()) {
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
                                text = "Nessun dipartimento configurato",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog per aggiungere nuova area
    if (showAddDialog) {
        AreaLavoroDialog(
            area = null,
            onDismiss = { companyVm.setShowAddDialog(false) },
            onConfirm = { nomeArea ->
                val nuovaArea = AreaLavoro(nomeArea = nomeArea)
                companyVm.addAreaModificata(nuovaArea)
                companyVm.setShowAddDialog(false)            }
        )
    }

    // Dialog per modificare area esistente
    if (editingArea != null) {
        AreaLavoroDialog(
            area = editingArea,
            onDismiss = { companyVm.setEditingArea(null) },
            onConfirm = { nomeArea ->
                companyVm.updateAreaModificata(editingArea!!.id, nomeArea)
                companyVm.setEditingArea(null)            }
        )
    }
}


@Composable
private fun AreaItemEditable(
    area: AreaLavoro,
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
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = area.nomeArea,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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

