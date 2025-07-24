package com.bizsync.app.screens




import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.User
import com.bizsync.ui.viewmodels.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDepartmentManagement(
    companyVm: CompanyViewModel,
    idAzienda: String,
    nuoviDipartimenti: List<AreaLavoro>,
    onBackClick: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by companyVm.uiState.collectAsState()
    val dipendenti = uiState.dipendenti
    val dipendentiModificati = uiState.dipendentiModificati
    val isLoading = uiState.isLoading
    val showDipartimentoDialog = uiState.showDipartimentoDialog
    val selectedDipendente = uiState.selectedDipendente

    // Carica tutti i dipendenti al primo caricamento
    LaunchedEffect(Unit) {
        companyVm.loadDipendentiAzienda(idAzienda)
    }

    // Verifica quali dipendenti hanno dipartimenti non più esistenti
    val dipendentiConProblemi = remember(dipendenti, nuoviDipartimenti) {
        dipendenti.filter { dipendente ->
            nuoviDipartimenti.none { dipartimento ->
                dipartimento.nomeArea == dipendente.dipartimento
            }
        }
    }

    val hasProblemi = dipendentiConProblemi.isNotEmpty()
    val hasModifiche = dipendentiModificati.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestione Dipendenti")
                        if (hasProblemi) {
                            Text(
                                text = "${dipendentiConProblemi.size} dipendenti richiedono attenzione",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (hasModifiche || hasProblemi) {
                        IconButton(
                            onClick = {
                                companyVm.salvaDipendentiModificati(idAzienda, onSaveComplete)
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Save, contentDescription = "Salva")
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Banner informativo se ci sono problemi
            if (hasProblemi) {
                item {
                    ProblemiDipartimentiWarning(
                        numProblemi = dipendentiConProblemi.size
                    )
                }
            }

            // Statistiche
            item {
                StatisticheDipendenti(
                    totaleDipendenti = dipendenti.size,
                    dipendentiConProblemi = dipendentiConProblemi.size,
                    dipendentiModificati = dipendentiModificati.size
                )
            }

            // Lista dipendenti
            items(
                items = dipendenti,
                key = { it.uid }
            ) { dipendente ->
                val haProblema = dipendentiConProblemi.contains(dipendente)
                val isModificato = dipendentiModificati.containsKey(dipendente.uid)
                val dipartimentoAttuale = if (isModificato) {
                    dipendentiModificati[dipendente.uid]?.dipartimento ?: dipendente.dipartimento
                } else {
                    dipendente.dipartimento
                }

                DipendenteItem(
                    dipendente = dipendente,
                    dipartimentoAttuale = dipartimentoAttuale,
                    haProblema = haProblema,
                    isModificato = isModificato,
                    nuoviDipartimenti = nuoviDipartimenti,
                    onChangeDipartimento = {
                        companyVm.setSelectedDipendente(dipendente)
                        companyVm.setShowDipartimentoDialog(true)
                    },
                    enabled = !isLoading
                )
            }

            // Messaggio se non ci sono dipendenti
            if (dipendenti.isEmpty() && !isLoading) {
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nessun dipendente trovato",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog per selezione dipartimento
    if (showDipartimentoDialog && selectedDipendente != null) {
        DipartimentoSelectionDialog(
            dipendente = selectedDipendente,
            dipartimenti = nuoviDipartimenti,
            onDismiss = { companyVm.setShowDipartimentoDialog(false) },
            onConfirm = { nuovoDipartimento ->
                companyVm.updateDipartimentoDipendente(
                    selectedDipendente.uid,
                    nuovoDipartimento
                )
                companyVm.setShowDipartimentoDialog(false)
            }
        )
    }
}

@Composable
fun ProblemiDipartimentiWarning(numProblemi: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.error,
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Attenzione richiesta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "$numProblemi dipendenti sono assegnati a dipartimenti che non esistono più e richiedono una riassegnazione.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun StatisticheDipendenti(
    totaleDipendenti: Int,
    dipendentiConProblemi: Int,
    dipendentiModificati: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticaItem(
                label = "Totale",
                value = totaleDipendenti.toString(),
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            StatisticaItem(
                label = "Con Problemi",
                value = dipendentiConProblemi.toString(),
                icon = Icons.Default.Warning,
                color = if (dipendentiConProblemi > 0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )

            StatisticaItem(
                label = "Modificati",
                value = dipendentiModificati.toString(),
                icon = Icons.Default.Edit,
                color = if (dipendentiModificati > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun StatisticaItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun DipendenteItem(
    dipendente: User,
    dipartimentoAttuale: String,
    haProblema: Boolean,
    isModificato: Boolean,
    nuoviDipartimenti: List<AreaLavoro>,
    onChangeDipartimento: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (haProblema) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(8.dp)
                    )
                } else if (isModificato) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                haProblema -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                isModificato -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Prima riga: Info dipendente
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
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = when {
                            haProblema -> MaterialTheme.colorScheme.error
                            isModificato -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Column {
                        Text(
                            text = "${dipendente.nome} ${dipendente.cognome}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = dipendente.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Indicatori di stato
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (haProblema) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Problema",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (isModificato) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modificato",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Seconda riga: Gestione dipartimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dipartimento attuale:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dipartimentoAttuale,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (haProblema)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    if (haProblema) {
                        Text(
                            text = "Dipartimento non più esistente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedButton(
                    onClick = onChangeDipartimento,
                    enabled = enabled
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (haProblema) "Riassegna" else "Cambia"
                    )
                }
            }
        }
    }
}

@Composable
fun DipartimentoSelectionDialog(
    dipendente: User,
    dipartimenti: List<AreaLavoro>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedDipartimento by remember { mutableStateOf(dipendente.dipartimento) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Riassegna Dipartimento")
        },
        text = {
            Column {
                Text(
                    text = "Dipendente: ${dipendente.nome} ${dipendente.cognome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Dipartimento attuale: ${dipendente.dipartimento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seleziona nuovo dipartimento:",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(dipartimenti) { dipartimento ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDipartimento == dipartimento.nomeArea,
                                onClick = { selectedDipartimento = dipartimento.nomeArea }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dipartimento.nomeArea,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDipartimento) },
                enabled = selectedDipartimento.isNotEmpty()
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}