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
                1 -> StatisticsContent(absenceState.absences)
                2 -> NewAbsenceRequestScreen(                      // adesso riceve fillMaxSize
                    userVM,
                    absenceVM,
                    onDismiss = { absenceVM.setSelectedTab(0) },
                    onSubmit  = { absenceVM.setSelectedTab(0) }
                )
            }
        }

    }




//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color(0xFFF5F5F5))
//                    .padding(bottom = 80.dp) // spazio per il FAB
//            ) {
//                TabRow(selectedTabIndex = absenceState.selectedTab) {
//                    Tab(
//                        selected = absenceState.selectedTab == 0,
//                        onClick = { absenceVM.setSelectedTab(0) },
//                        text = { Text("Le Mie Richieste") }
//                    )
//                    Tab(
//                        selected = absenceState.selectedTab == 1,
//                        onClick = { absenceVM.setSelectedTab(1) },
//                        text = { Text("Statistiche") }
//                    )
//                }


}




//        }

//}



@Composable
private fun RequestsListContent(absenceVM : AbsenceViewModel) {

    val uiState by absenceVM.uiState.collectAsState()
    val requests = uiState.absences

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tutto il contenuto della schermata, ad esempio:
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // spazio per il FAB se serve
        ) {
            // ... contenuti vari
        }

        // FAB in basso a destra
        FloatingActionButton(
            onClick = { absenceVM.setSelectedTab(2) },
            modifier = Modifier
                .align(Alignment.BottomEnd) // <<< ECCO QUI
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nuova richiesta")
        }
    }

    if (requests.isEmpty()) {
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
    else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                AbsenceRequestCard(request = request)
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

@Composable
private fun StatisticsContent(requests: List<AbsenceUi>) {
//    val approvedRequests = requests.filter { it.statusUi.status == AbsenceStatus.APPROVED }
//    val vacationDays = approvedRequests.filter { it.typeUi.type == AbsenceType.VACATION }.sumOf { it.totalDays }
//    val sickDays = approvedRequests.filter { it.typeUi.type == AbsenceType.SICK_LEAVE }.sumOf { it.totalDays }
//    val rolDays = approvedRequests.filter { it.typeUi.type == AbsenceType.ROL }.sumOf { it.totalDays }
//
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        item {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        "Riepilogo Anno Corrente",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    StatisticRow("Ferie utilizzate", "$vacationDays giorni", AbsenceType.VACATION.color)
//                    StatisticRow("Giorni malattia", "$sickDays giorni", AbsenceType.SICK_LEAVE.color)
//                    StatisticRow("Permessi ROL", "$rolDays giorni", AbsenceType.ROL.color)
//                }
//            }
//        }
//    }
}

@Composable
private fun StatisticRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}




