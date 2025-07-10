package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.TimeButton
import com.bizsync.ui.components.DatePickerDialog
import com.bizsync.ui.components.DateButton
import com.bizsync.ui.components.StatusDialog

import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceStatusUi
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.AbsenceViewModel
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsencesManagementScreen(onBackClick: () -> Unit) {
    val absenceVM: AbsenceViewModel = hiltViewModel()
    val absenceState by absenceVM.uiState.collectAsState()
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()

    val contratto = userState.contratto

    LaunchedEffect(Unit) {
        absenceVM.fetchAllAbsences(userState.user.uid)
    }

    if (!absenceState.hasLoadedAbsences) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    else{
        Box(modifier = Modifier.fillMaxSize()) {               // ← qui
            when (absenceState.selectedTab) {
                0 -> RequestsListContent(absenceVM)
                2 -> NewAbsenceRequestScreen(
                    userVM,
                    absenceVM,
                    onDismiss = { absenceVM.setSelectedTab(0) },
                    onSubmit  = { absenceVM.setSelectedTab(0) }
                )
            }
        }

    }


}


@Composable
private fun RequestsListContent(absenceVM: AbsenceViewModel) {
    val uiState by absenceVM.uiState.collectAsState()
    val requests = uiState.absences

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val contratto = userState.contratto

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (requests.isEmpty()) {
            // Stato vuoto - mostra solo le statistiche e il messaggio
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Card statistiche anche quando è vuoto
                contratto?.let {
                    StatisticsCard(contratto = it)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Messaggio di lista vuota
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Nessuna richiesta di assenza",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            // Lista con richieste - LazyColumn con statistiche in cima
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp), // spazio per il FAB
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Prima item: Card delle statistiche
                item {
                    contratto?.let {
                        StatisticsCard(contratto = it)
                    }
                }

                // Divider tra statistiche e richieste
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Le Mie Richieste",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .weight(1f)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }

                // Lista delle richieste
                items(requests) { request ->
                    AbsenceRequestCard(request = request)
                }
            }
        }

        // FAB sempre visibile
        FloatingActionButton(
            onClick = { absenceVM.setSelectedTab(2) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nuova richiesta")
        }
    }
}

@Composable
private fun StatisticsCard(contratto: Contratto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Riepilogo Anno Corrente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ferie
            StatisticProgressRow(
                label = "Ferie",
                used = contratto.ferieUsate,
                max = contratto.ccnlInfo.ferieAnnue,
                unit = "giorni",
                color = Color(0xFF4CAF50),
                icon = Icons.Default.BeachAccess
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ROL
            StatisticProgressRow(
                label = "Permessi ROL",
                used = contratto.rolUsate,
                max = contratto.ccnlInfo.rolAnnui,
                unit = "ore",
                color = Color(0xFF2196F3),
                icon = Icons.Default.Schedule
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Malattia
            StatisticProgressRow(
                label = "Malattia",
                used = contratto.malattiaUsata,
                max = contratto.ccnlInfo.malattiaRetribuita,
                unit = "giorni",
                color = Color(0xFFFF9800),
                icon = Icons.Default.LocalHospital
            )
        }
    }
}

@Composable
private fun StatisticProgressRow(
    label: String,
    used: Int,
    max: Int,
    unit: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "$used/$max $unit",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra di progresso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(3.dp)
                )
        ) {
            val progress = if (max > 0) (used.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .background(
                        color,
                        RoundedCornerShape(3.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Percentuale e rimanenti
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val percentage = if (max > 0) ((used.toFloat() / max.toFloat()) * 100).toInt() else 0
            val remaining = (max - used).coerceAtLeast(0)

            Text(
                "$percentage% utilizzato",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                "Rimangono $remaining $unit",
                fontSize = 11.sp,
                color = when {
                    remaining == 0 -> Color(0xFFD32F2F)
                    remaining <= max * 0.2 -> Color(0xFFFF9800)
                    else -> Color.Gray
                }
            )
        }
    }
}

@Composable
private fun AbsenceRequestCard(request: AbsenceUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        request.typeUi.icon,
                        contentDescription = null,
                        tint = request.typeUi.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        request.typeUi.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                StatusChip(request.statusUi)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date range
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = request.formattedDateRange)

            }

            request.formattedHours?.let {
                Text(text = it)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total days and reason
            Text(
                "Giorni totali: ${request.totalDays}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (request.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Motivo: ${request.reason}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Approval info
            if (request.statusUi.status == AbsenceStatus.APPROVED && request.approver != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Approvata da: ${request.approver} il ${request.approvedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: AbsenceStatusUi) {
    Box(
        modifier = Modifier
            .background(
                status.color.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                status.color.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            status.displayName,
            color = status.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}



