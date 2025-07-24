package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.model.Turno
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.viewmodels.EmployeeManagementViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(
    employee: UserUi,
    employeeVm: EmployeeManagementViewModel,
    isHistorical: Boolean = false,
) {
    val uiState by employeeVm.uiState.collectAsState()
    val shifts = uiState.shifts
    val isLoading = uiState.isLoading

    val userVm = LocalUserViewModel.current
    val userState by userVm.uiState.collectAsState()

    var selectedPeriod by remember { mutableStateOf(HistoricalPeriod.LAST_MONTH) }
    var customStartDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var customEndDate by remember { mutableStateOf(LocalDate.now()) }

    // Calcola il range di date basato sul periodo selezionato
    val dateRange = remember(selectedPeriod, customStartDate, customEndDate) {
        when (selectedPeriod) {
            HistoricalPeriod.LAST_WEEK -> {
                val end = LocalDate.now()
                val start = end.minusWeeks(1)
                Pair(start, end)
            }
            HistoricalPeriod.LAST_MONTH -> {
                val end = LocalDate.now()
                val start = end.minusMonths(1)
                Pair(start, end)
            }
            HistoricalPeriod.LAST_3_MONTHS -> {
                val end = LocalDate.now()
                val start = end.minusMonths(3)
                Pair(start, end)
            }
            HistoricalPeriod.CUSTOM -> Pair(customStartDate, customEndDate)
        }
    }

    // Carica i turni quando cambia il periodo o il dipendente
    LaunchedEffect(employee.uid, isHistorical, dateRange) {
        if (isHistorical) {
            employeeVm.loadEmployeePastShifts(
                employeeId = employee.uid,
                startDate = dateRange.first,
                endDate = dateRange.second,
                idAzienda = userState.azienda.idAzienda
            )
        } else {
            employeeVm.loadEmployeeFutureShifts(employee.uid, userState.azienda.idAzienda)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = if (isHistorical) "Turni Passati" else "Turni Futuri",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            },
            navigationIcon = {
                IconButton(onClick = { employeeVm.setCurrentSection(EmployeeSection.MAIN) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        )

        // Card con info dipendente
        EmployeeInfoCard(employee = employee, isHistorical = isHistorical)

        // Filtri per turni storici
        if (isHistorical) {
            HistoricalFiltersCard(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                dateRange = dateRange,
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            shifts.isEmpty() -> {
                EmptyShiftsState(isHistorical = isHistorical, dateRange = dateRange)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header con statistiche
                    item {
                        ShiftsStatsCard(shifts = shifts, isHistorical = isHistorical)
                    }

                    // Lista turni
                    items(shifts) { shift ->
                        ShiftCard(shift = shift, isHistorical = isHistorical)
                    }

                    if (isHistorical && shifts.isNotEmpty()) {
                        item {
                            HistoricalInfoCard(dateRange = dateRange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeInfoCard(employee: UserUi, isHistorical: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                        else Color(0xFFF39C12).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isHistorical) Icons.Default.History else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${employee.nome} ${employee.cognome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = employee.posizioneLavorativa,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D)
                )
            }
        }
    }
}

@Composable
fun HistoricalFiltersCard(
    selectedPeriod: HistoricalPeriod,
    onPeriodSelected: (HistoricalPeriod) -> Unit,
    dateRange: Pair<LocalDate, LocalDate>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF2C3E50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Periodo di ricerca",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoricalPeriod.entries.toTypedArray()) { period ->
                    FilterChip(
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.displayName) },
                        selected = selectedPeriod == period,
                        leadingIcon = if (selectedPeriod == period) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Info periodo selezionato
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dal ${dateRange.first.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} " +
                        "al ${dateRange.second.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                fontSize = 12.sp,
                color = Color(0xFF7F8C8D)
            )
        }
    }
}

@Composable
fun ShiftsStatsCard(shifts: List<Turno>, isHistorical: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = if (isHistorical) Icons.Default.CheckCircle else Icons.Default.Schedule,
                value = shifts.size.toString(),
                label = if (isHistorical) "Turni completati" else "Turni programmati",
                color = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12)
            )

        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF7F8C8D)
        )
    }
}

@Composable
fun HistoricalInfoCard(dateRange: Pair<LocalDate, LocalDate>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF3498DB),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dati caricati da Firebase per il periodo selezionato",
                fontSize = 12.sp,
                color = Color(0xFF7F8C8D)
            )
        }
    }
}

@Composable
fun ShiftCard(shift: Turno, isHistorical: Boolean) {


    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = shift.data.format(formatter)


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona del turno
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                        else Color(0xFFF39C12).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informazioni del turno
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift.titolo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${shift.orarioInizio} - ${shift.orarioFine}",
                    fontSize = 12.sp,
                    color = Color(0xFF95A5A6),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Badge stato
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                    else Color(0xFFF39C12).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isHistorical) "Completato" else "Programmato",
                    fontSize = 12.sp,
                    color = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyShiftsState(isHistorical: Boolean, dateRange: Pair<LocalDate, LocalDate>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isHistorical) Icons.Default.History else Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDC3C7)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isHistorical) "Nessun turno trovato" else "Nessun turno programmato",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7F8C8D)
            )

            val subtitle = if (isHistorical) {
                "Nessun turno trovato nel periodo selezionato:\n" +
                        "${dateRange.first.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - " +
                        "${dateRange.second.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
            } else {
                "Non ci sono turni futuri programmati"
            }

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFFBDC3C7),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

enum class HistoricalPeriod(val displayName: String) {
    LAST_WEEK("Ultima settimana"),
    LAST_MONTH("Ultimo mese"),
    LAST_3_MONTHS("Ultimi 3 mesi"),
    CUSTOM("Personalizzato")
}