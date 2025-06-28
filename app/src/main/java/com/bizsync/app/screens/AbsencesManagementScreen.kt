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
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.TimeButton
import com.bizsync.ui.components.DatePickerDialog
import com.bizsync.ui.components.DateButton

import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceStatusUi
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.AbsenceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsencesManagementScreen(onBackClick: () -> Unit) {
    var showNewRequestDialog by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    val absenceVM : AbsenceViewModel = hiltViewModel()

    val absenceState by absenceVM.uiState.collectAsState()

    val absenceRequests = absenceState.absences

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Le Mie Richieste") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Statistiche") }
            )
        }

        when (selectedTab) {
            0 -> RequestsListContent(absenceRequests)
            1 -> StatisticsContent(absenceRequests)
        }
    }

    // Dialog per nuova richiesta
    if (showNewRequestDialog) {
        NewAbsenceRequestScreen(
            absenceVM,
            onDismiss = { showNewRequestDialog = false },
            onSubmit = { showNewRequestDialog = false }
        )
    }
}

@Composable
private fun RequestsListContent(requests: List<AbsenceUi>) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAbsenceRequestScreen(
    absenceVM: AbsenceViewModel,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val scaffoldVM = LocalScaffoldViewModel.current
    val uiState by absenceVM.uiState.collectAsState()
    val addAbsence = uiState.addAbsence

    var isFullDay = uiState.isFullDay
    var showStartDatePicker = uiState.showStartDatePicker
    var showEndDatePicker = uiState.showEndDatePicker

    val status = uiState.statusMsg

    LaunchedEffect(status) {
        if (status is Resource.Success<*>) {
            onSubmit()
        }
    }


    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)

    }


    LaunchedEffect(addAbsence.startDate, addAbsence.endDate, addAbsence.startTime, addAbsence.endTime, isFullDay) {
        val startDate = addAbsence.startDate
        val endDate = addAbsence.endDate
        val startTime = addAbsence.startTime
        val endTime = addAbsence.endTime

        if (startDate != null && endDate != null) {
            val totalDays = absenceVM.calculateTotalDays(startDate, endDate, isFullDay, startTime, endTime)
            absenceVM.updateAddAbsenceTotalDays(totalDays)
        } else {
            absenceVM.updateAddAbsenceTotalDays("0 giorni")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuova Richiesta Assenza") },
                navigationIcon = {
                    IconButton(onClick = {
                        absenceVM.resetAddAbsence()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            absenceVM.saveAbsence()

                        },
                        enabled = (addAbsence.startDate != null && addAbsence.endDate != null) &&
                                addAbsence.reason.isNotEmpty() &&
                                (isFullDay || (addAbsence.startTime != null && addAbsence.endTime != null))
                    ) {
                        Text("INVIA", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Absence Type Selector
            item {
                ModernAbsenceTypeSelector(
                    selectedType = addAbsence.typeUi,
                    onTypeSelected = { selectedType ->
                        selectedType?.let { absenceVM.updateAddAbsenceType(it) }
                    }
                )
            }

            // Date Selection Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Periodo di assenza",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        // Date buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Date
                            // Bottone per la data di inizio
                            DateButton(
                                label = "Data inizio",
                                selectedDate = addAbsence.startDate,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    absenceVM.setShowStartDatePicker(true)

                                }
                            )

                            DateButton(
                                label = "Data fine",
                                selectedDate = addAbsence.endDate,
                                modifier = Modifier.weight(1f),
                                onClick = {

                                    absenceVM.setShowEndDatePicker(true)
                                }
                            )

                        }

                        // Full day toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Giornata intera",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = isFullDay,
                                onCheckedChange = { newValue ->
                                    isFullDay = newValue
                                    if (newValue) {
                                        absenceVM.updateAddAbsenceStartTime(null)
                                        absenceVM.updateAddAbsenceEndTime(null)
                                    }
                                }
                            )
                        }

                        // Time selection (if not full day)
                        if (!isFullDay) {
                            TimeRangePicker(
                                startTime = addAbsence.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                endTime = addAbsence.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                onStartTimeSelected = { timeString ->
                                    val localTime = try {
                                        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                                    } catch (e: Exception) {
                                        null
                                    }
                                    absenceVM.updateAddAbsenceStartTime(localTime)
                                },
                                onEndTimeSelected = { timeString ->
                                    val localTime = try {
                                        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                                    } catch (e: Exception) {
                                        null
                                    }
                                    absenceVM.updateAddAbsenceEndTime(localTime)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Total days display
                        if (addAbsence.totalDays != "0 giorni") {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = "Totale: ${addAbsence.totalDays}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Reason and Comments
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Dettagli richiesta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        OutlinedTextField(
                            value = addAbsence.reason,
                            onValueChange = { absenceVM.updateAddAbsenceReason(it) },
                            label = { Text("Motivo *") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = addAbsence.comments ?: "",
                            onValueChange = { absenceVM.updateAddAbsenceComments(it) },
                            label = { Text("Note aggiuntive") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    absenceVM.updateAddAbsenceStartDate(selectedDate)

                    // Se l’endDate è prima dello startDate, la aggiorno
                    if (addAbsence.endDate != null && addAbsence.endDate!!.isBefore(selectedDate)) {
                        absenceVM.updateAddAbsenceEndDate(selectedDate)
                    }
                }
                absenceVM.setShowStartDatePicker(false)
            },
            onDismiss = {
                absenceVM.setShowStartDatePicker(false)
            },
            initialDate = addAbsence.startDate
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    // Aggiorno endDate solo se è dopo lo startDate
                    if (addAbsence.startDate == null || !selectedDate.isBefore(addAbsence.startDate)) {
                        absenceVM.updateAddAbsenceEndDate(selectedDate)
                    }
                }
                absenceVM.setShowEndDatePicker(false)
                             },
            onDismiss = {
                absenceVM.setShowEndDatePicker(false)
            },
            initialDate = addAbsence.endDate ?: addAbsence.startDate
        )
    }



    // Handle result messages
    uiState.resultMsg?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            absenceVM.clearResultMessage()

            if (uiState.statusMsg == DialogStatusType.SUCCESS) {
                onDismiss()
            }
        }
    }
}



@Composable
private fun ModernAbsenceTypeSelector(
    selectedType: AbsenceTypeUi?,
    onTypeSelected: (AbsenceTypeUi) -> Unit
) {
    val absenceTypes = remember {
        AbsenceType.entries.map { it.toUiData() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tipo di assenza",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(absenceTypes) { type ->
                    FilterChip(
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) },
                        selected = selectedType?.type == type.type,
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedType?.type == type.type) Icons.Default.Check else type.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedType?.type == type.type) type.color else Color.Gray
                            )
                        }
                    )
                }
            }
        }
    }
}




