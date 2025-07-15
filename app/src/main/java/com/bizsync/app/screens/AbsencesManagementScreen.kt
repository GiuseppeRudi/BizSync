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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceStatusUi
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.model.PendingStats
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
        Box(modifier = Modifier.fillMaxSize()) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Card statistiche anche quando è vuoto
                contratto?.let {
                    StatisticsCard(contratto = it, absenceVM )
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
                        StatisticsCard(contratto = it, absenceVM)
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
// Aggiorna StatisticsCard per calcolare anche le richieste pending
@Composable
private fun StatisticsCard(contratto: Contratto, absenceVM: AbsenceViewModel) {

    val uiState by absenceVM.uiState.collectAsState()

   val pendingStats = uiState.pendingStats

    LaunchedEffect(uiState.absences) {
        absenceVM.changePendingStatus()    }

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
            EnhancedStatisticProgressRow(
                label = "Ferie",
                approved = contratto.ferieUsate,
                pending = pendingStats.pendingVacationDays,
                max = contratto.ccnlInfo.ferieAnnue,
                unit = "giorni",
                color = Color(0xFF4CAF50),
                icon = Icons.Default.BeachAccess
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ROL
            EnhancedStatisticProgressRow(
                label = "Permessi ROL",
                approved = contratto.rolUsate,
                pending = pendingStats.pendingRolHours,
                max = contratto.ccnlInfo.rolAnnui,
                unit = "ore",
                color = Color(0xFF2196F3),
                icon = Icons.Default.Schedule
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Malattia
            EnhancedStatisticProgressRow(
                label = "Malattia",
                approved = contratto.malattiaUsata,
                pending = pendingStats.pendingSickDays,
                max = contratto.ccnlInfo.malattiaRetribuita,
                unit = "giorni",
                color = Color(0xFFFF9800),
                icon = Icons.Default.LocalHospital
            )
        }
    }
}



// StatisticProgressRow potenziata con pending
@Composable
private fun EnhancedStatisticProgressRow(
    label: String,
    approved: Int,
    pending: Int,
    max: Int,
    unit: String,
    color: Color,
    icon: ImageVector
) {
    val total = approved + pending

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

            // Mostra approved + pending / max
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$approved",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (pending > 0) {
                    Text(
                        " + $pending",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800) // Arancione per pending
                    )
                }
                Text(
                    " / $max $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra di progresso con sezioni
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            // Progresso approvato (verde)
            val approvedProgress = if (max > 0) (approved.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
            if (approvedProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(approvedProgress)
                        .height(8.dp)
                        .background(
                            color,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            // Progresso pending (arancione) - si sovrappone a quello approvato
            val totalProgress = if (max > 0) (total.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
            if (pending > 0 && totalProgress > approvedProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(totalProgress)
                        .height(8.dp)
                        .background(
                            if (totalProgress > 1f) Color(0xFFD32F2F) else Color(0xFFFF9800),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dettagli con legenda
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Prima riga: percentuali e rimanenti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val approvedPercentage = if (max > 0) ((approved.toFloat() / max.toFloat()) * 100).toInt() else 0
                val totalPercentage = if (max > 0) ((total.toFloat() / max.toFloat()) * 100).toInt() else 0
                val remaining = (max - total).coerceAtLeast(0)

                Text(
                    "Utilizzato: $approvedPercentage%${if (pending > 0) " (+${totalPercentage - approvedPercentage}% pending)" else ""}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Text(
                    "Rimangono: $remaining $unit",
                    fontSize = 11.sp,
                    color = when {
                        remaining <= 0 -> Color(0xFFD32F2F)
                        remaining <= max * 0.2 -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                )
            }

            // Seconda riga: dettaglio approvato vs pending (solo se ci sono pending)
            if (pending > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Approvate: $approved $unit",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF9800), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "In attesa: $pending $unit",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
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

            // Hours se presenti
            request.formattedHours?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = it)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total days/hours - aggiornato per gestire nullable
            val totalText = when (request.typeUi.type) {
                AbsenceType.ROL -> {
                    request.formattedTotalHours ?: "0 ore"
                }
                else -> {
                    request.formattedTotalDays
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Totale: $totalText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (request.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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



