package com.bizsync.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.model.*
import com.bizsync.ui.viewmodels.ManagerTimbratureViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimbratureManagementScreen(
    onBackClick: () -> Unit,
    managerHome: ManagerTimbratureViewModel = hiltViewModel()
) {
    val uiState by managerHome.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf(FilterType.TUTTE) }
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val idAzienda = userState.user.idAzienda

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    LaunchedEffect(idAzienda, selectedDate) {
        managerHome.loadTimbrature(idAzienda, selectedDate, selectedDate)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let {
                    val newDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    selectedDate = newDate
                    managerHome.loadTimbrature(idAzienda, newDate, newDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            datePickerState = datePickerState
        )
    }

    // Dialoghi di errore e successo
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = managerHome::dismissError,
            title = { Text("Errore") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = managerHome::dismissError) {
                    Text("OK")
                }
            }
        )
    }

    if (uiState.showSuccess) {
        AlertDialog(
            onDismissRequest = managerHome::dismissSuccess,
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color.Green) },
            title = { Text("Successo") },
            text = { Text(uiState.successMessage) },
            confirmButton = {
                TextButton(onClick = managerHome::dismissSuccess) {
                    Text("OK")
                }
            }
        )
    }

    uiState.selectedTimbratura?.let { timbratura ->
        TimbraturaDetailDialog(
            timbratura = timbratura,
            onDismiss = { managerHome.clearSelectedTimbratura() },
            onVerifica = {
                managerHome.verificaTimbratura(timbratura.id)
                managerHome.clearSelectedTimbratura()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestione Timbrature") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            "Seleziona data",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM")),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    IconButton(
                        onClick = {
                            managerHome.loadTimbrature(idAzienda, selectedDate, selectedDate)
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Aggiorna")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header con statistiche
            TimbratureStatsCard(
                totali = uiState.timbrature.size,
                anomale = uiState.timbratureAnomale.size,
                daVerificare = uiState.timbratureDaVerificare.size,
                data = selectedDate,
                isLoading = uiState.isLoading
            )

            // Filtri
            FilterChips(
                selectedFilter = filterType,
                onFilterChange = { filterType = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista timbrature
            val filteredTimbrature = when (filterType) {
                FilterType.TUTTE -> uiState.timbrature
                FilterType.ANOMALE -> uiState.timbratureAnomale
                FilterType.DA_VERIFICARE -> uiState.timbratureDaVerificare
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Caricamento timbrature...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (filteredTimbrature.isEmpty()) {
                EmptyStateMessage(filterType)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTimbrature) { timbratura ->
                        TimbraturaCard(
                            timbratura = timbratura,
                            onClick = { managerHome.selectTimbratura(timbratura) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Seleziona data",
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                Text(
                    text = datePickerState.selectedDateMillis?.let { millis ->
                        LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                    } ?: "Nessuna data selezionata",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}

@Composable
fun TimbratureStatsCard(
    modifier: Modifier = Modifier,
    totali: Int,
    anomale: Int,
    daVerificare: Int,
    data: LocalDate,
    isLoading: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                Text(
                    text = data.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = if (isLoading) "-" else totali.toString(),
                    label = "Totali",
                    color = MaterialTheme.colorScheme.primary
                )

                StatItem(
                    value = if (isLoading) "-" else anomale.toString(),
                    label = "Anomale",
                    color = if (anomale > 0) Color.Red else MaterialTheme.colorScheme.tertiary
                )

                StatItem(
                    value = if (isLoading) "-" else daVerificare.toString(),
                    label = "Da verificare",
                    color = if (daVerificare > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterChips(
    selectedFilter: FilterType,
    onFilterChange: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterType.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun TimbraturaCard(
    timbratura: Timbratura,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when {
                timbratura.isAnomala() -> Color.Red.copy(alpha = 0.1f)
                !timbratura.verificataDaManager -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (timbratura.tipoTimbratura) {
                    TipoTimbratura.ENTRATA -> Icons.AutoMirrored.Filled.Login
                    TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.Logout
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (timbratura.isAnomala()) Color.Red
                else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dipendente: ${timbratura.idDipendente}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = timbratura.dataOraTimbratura.format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Indicatori anomalie
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!timbratura.dentroDellaTolleranza) {
                        Chip(
                            label = "Posizione",
                            color = Color.Red,
                            icon = Icons.Default.LocationOff
                        )
                    }

                    when (timbratura.statoTimbratura) {
                        StatoTimbratura.RITARDO_LIEVE -> {
                            Chip(
                                label = "${timbratura.minutiRitardo}m ritardo",
                                color = Color(0xFFFF9800),
                                icon = Icons.Default.Schedule
                            )
                        }
                        StatoTimbratura.RITARDO_GRAVE -> {
                            Chip(
                                label = "${timbratura.minutiRitardo}m ritardo",
                                color = Color.Red,
                                icon = Icons.Default.Schedule
                            )
                        }
                        else -> {}
                    }

                    if (timbratura.zonaLavorativa == ZonaLavorativa.IN_SEDE &&
                        timbratura.distanzaDallAzienda != null) {
                        Chip(
                            label = "${timbratura.distanzaDallAzienda!!.toInt()}m",
                            color = if (timbratura.dentroDellaTolleranza)
                                Color.Green else Color.Red,
                            icon = Icons.Default.MyLocation
                        )
                    }
                }
            }

            // Stato verifica
            Icon(
                imageVector = if (timbratura.verificataDaManager)
                    Icons.Default.Verified else Icons.Default.PendingActions,
                contentDescription = null,
                tint = if (timbratura.verificataDaManager)
                    Color.Green else Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun Chip(
    label: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun TimbraturaDetailDialog(
    timbratura: Timbratura,
    onDismiss: () -> Unit,
    onVerifica: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Dettaglio Timbratura")
        },
        text = {
            Column {
                DetailRow("Tipo", timbratura.tipoTimbratura.name)
                DetailRow("Orario", timbratura.dataOraTimbratura.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                ))
                DetailRow("Orario previsto", timbratura.dataOraPrevista.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                ))
                DetailRow("Stato", timbratura.statoTimbratura.name)

                if (timbratura.minutiRitardo > 0) {
                    DetailRow("Ritardo", "${timbratura.minutiRitardo} minuti")
                }

                DetailRow("Zona lavorativa", timbratura.zonaLavorativa.name)

                if (timbratura.distanzaDallAzienda != null) {
                    DetailRow(
                        "Distanza dall'azienda",
                        "${timbratura.distanzaDallAzienda!!.toInt()} metri"
                    )
                    DetailRow(
                        "Posizione",
                        if (timbratura.dentroDellaTolleranza) "Corretta" else "Fuori tolleranza"
                    )
                }

                if (timbratura.note.isNotEmpty()) {
                    DetailRow("Note", timbratura.note)
                }
            }
        },
        confirmButton = {
            if (!timbratura.verificataDaManager) {
                Button(onClick = onVerifica) {
                    Text("VERIFICA")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CHIUDI")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyStateMessage(filterType: FilterType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Green
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filterType) {
                    FilterType.ANOMALE -> "Nessuna timbratura anomala"
                    FilterType.DA_VERIFICARE -> "Tutte le timbrature sono state verificate"
                    else -> "Nessuna timbratura per questa data"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class FilterType(val label: String) {
    TUTTE("Tutte"),
    ANOMALE("Anomale"),
    DA_VERIFICARE("Da verificare")
}