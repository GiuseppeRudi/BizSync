// 3. AGGIORNA DipartimentiManagementScreen.kt
package com.bizsync.app.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.AreaLavoroDialog
import com.bizsync.ui.viewmodels.CompanyViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DipartimentiManagementScreen(
    companyVm: CompanyViewModel,
    areeLavoro: List<AreaLavoro>,
    idAzienda: String,
    orariSettimanali: Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>>
) {

    Log.d("ORARI ", orariSettimanali.toString())

    val uiState by companyVm.uiState.collectAsState()
    val scaffoldVM = LocalScaffoldViewModel.current

    val areeModificate = uiState.areeModificate
    val orariModificati = uiState.orariSettimanaliModificati
    val showAddDialog = uiState.showAddDialog
    val editingArea = uiState.editingArea
    val showOrariDialog = uiState.showOrariDialog
    val editingOrariAreaId = uiState.editingOrariAreaId
    val orariTemp = uiState.orariTemp
    val hasChanges = uiState.hasChanges
    val isLoading = uiState.isLoading

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
        companyVm.setAreeModificate(areeLavoro)
        companyVm.setOrariSettimanaliModificati(orariSettimanali)
    }

    LaunchedEffect(areeModificate, orariModificati) {
        val hasDiff = (uiState.areeModificate != areeLavoro) || (uiState.orariSettimanaliModificati != orariSettimanali)
        companyVm.setHasChanges(hasDiff)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar (stesso codice esistente)
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        // Lista delle aree CON GESTIONE ORARI
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
                AreaItemEditableWithOrari(
                    area = area,
                    orariConfigurati = orariModificati[area.id]?.isNotEmpty() == true,
                    onEdit = { companyVm.setEditingArea(area) },
                    onDelete = { companyVm.removeAreaModificata(area) },
                    onEditOrari = { companyVm.openOrariDialog(area.id) },
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
                companyVm.setShowAddDialog(false)
            }
        )
    }

    // Dialog per modificare area esistente
    if (editingArea != null) {
        AreaLavoroDialog(
            area = editingArea,
            onDismiss = { companyVm.setEditingArea(null) },
            onConfirm = { nomeArea ->
                companyVm.updateAreaModificata(editingArea.id, nomeArea)
                companyVm.setEditingArea(null)
            }
        )
    }

    // NUOVO DIALOG PER GESTIONE ORARI
    if (showOrariDialog && editingOrariAreaId != null) {
        OrariSettimanaliDialog(companyVm)
    }
}

@Composable
private fun AreaItemEditableWithOrari(
    area: AreaLavoro,
    orariConfigurati: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditOrari: () -> Unit,
    enabled: Boolean
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
            // Prima riga: Nome area e azioni principali
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

            // Seconda riga: Gestione orari
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (orariConfigurati)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = if (orariConfigurati) "Orari configurati" else "Orari non configurati",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (orariConfigurati)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (orariConfigurati) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Configurati",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditOrari,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (orariConfigurati) "Modifica Orari" else "Configura Orari")
                }
            }
        }
    }
}

@Composable
fun TimePickerField(
    value: LocalTime,
    onValueChange: (LocalTime) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.format(DateTimeFormatter.ofPattern("HH:mm")),
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier.clickable { showTimePicker = true },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Seleziona orario")
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = value,
            onTimeSelected = { newTime ->
                onValueChange(newTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleziona orario") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrariSettimanaliDialog(
    companyVm: CompanyViewModel
) {
    val giorni = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    val uiState by companyVm.uiState.collectAsState()
    val areeModificate = uiState.areeModificate
    val orariTemp = uiState.orariTemp
    val editingOrariAreaId = uiState.editingOrariAreaId

    val orarisettimanali = uiState.orariSettimanaliModificati

    LaunchedEffect(Unit) {
        companyVm.onChangedOrariTemp(orarisettimanali[editingOrariAreaId])
    }

    val nomeArea = areeModificate.find { it.id == editingOrariAreaId }?.nomeArea ?: ""

    AlertDialog(
        onDismissRequest = {companyVm.closeOrariDialog()},
        title = {
            Text("Orari Settimanali - $nomeArea")
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(giorni) { giorno ->
                    val nomeGiorno = giorno.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
                    val orarioGiorno = orariTemp[giorno]

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = orarioGiorno != null,
                                onCheckedChange = { isChecked ->
                                    companyVm.onGiornoLavoroChanged(giorno, isChecked)
                                }
                            )

                            Text(
                                text = nomeGiorno.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (orarioGiorno != null) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        if (orarioGiorno != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TimePickerField(
                                    value = orarioGiorno.first,
                                    onValueChange = { companyVm.onOrarioInizioChanged(giorno, it) },
                                    label = "Inizio",
                                    modifier = Modifier.weight(1f)
                                )

                                TimePickerField(
                                    value = orarioGiorno.second,
                                    onValueChange = { companyVm.onOrarioFineChanged(giorno, it) },
                                    label = "Fine",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { companyVm.salvaOrariArea() },
                enabled = orariTemp.isNotEmpty()
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {companyVm.closeOrariDialog()}) {
                Text("Annulla")
            }
        }
    )
}