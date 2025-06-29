package com.bizsync.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.components.DateButton
import com.bizsync.ui.components.DatePickerDialog
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.components.TimePickerField
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.theme.BizSyncColors.Surface
import com.bizsync.ui.viewmodels.AbsenceViewModel
import com.bizsync.ui.viewmodels.UserViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAbsenceRequestScreen(
    userVM: UserViewModel,
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
    val userState by userVM.uiState.collectAsState()
    val idAzienda = userState.azienda.idAzienda
    val fullName = userState.user.cognome + " " + userState.user.nome
    val idUser = userState.user.uid

    LaunchedEffect(status) {
        if (status == DialogStatusType.SUCCESS) {
            scaffoldVM.onFullScreenChanged(true)
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

    // Logica di validazione migliorata per il pulsante INVIA
    val isValidSubmission = remember(addAbsence.startDate, addAbsence.endDate, addAbsence.reason, isFullDay, addAbsence.startTime, addAbsence.endTime) {
        val hasValidDates = addAbsence.startDate != null && addAbsence.endDate != null
        val hasValidReason = addAbsence.reason.isNotEmpty()

        if (!hasValidDates || !hasValidReason) {
            false
        } else if (isFullDay) {
            true
        } else {
            val hasValidTimes = addAbsence.startTime != null && addAbsence.endTime != null

            if (!hasValidTimes) {
                false
            } else {
                val isSingleDay = addAbsence.startDate == addAbsence.endDate

                if (isSingleDay) {
                    addAbsence.endTime!!.isAfter(addAbsence.startTime!!)
                } else {
                    true
                }
            }
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
                            absenceVM.saveAbsence(fullName,idAzienda, idUser)
                        },
                        enabled = isValidSubmission
                    ) {
                        Text("INVIA", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    ) { paddingValues ->
        // SOLUZIONE 1: Usa Column con verticalScroll invece di LazyColumn
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()) // Questo risolve il problema di scroll
                .imePadding(), // Aggiunto per evitare problemi con la tastiera
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Spacer iniziale
            Spacer(modifier = Modifier.height(16.dp))

            // Absence Type Selector
            ModernAbsenceTypeSelector(
                selectedType = addAbsence.typeUi,
                onTypeSelected = { selectedType ->
                    selectedType?.let { absenceVM.updateAddAbsenceType(it) }
                }
            )

            // Date Selection Section
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
                                absenceVM.setIsFullDay(newValue)
                                if (newValue) {
                                    absenceVM.updateAddAbsenceStartTime(null)
                                    absenceVM.updateAddAbsenceEndTime(null)
                                }
                            }
                        )
                    }

                    // Helper functions
                    val isSingleDay = addAbsence.startDate != null && addAbsence.endDate != null &&
                            addAbsence.startDate == addAbsence.endDate

                    val shouldShowTimeSection = addAbsence.startDate != null && addAbsence.endDate != null && !isFullDay

                    // Time selection section
                    if (shouldShowTimeSection) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isSingleDay) {
                                            "Assenza giornaliera parziale"
                                        } else {
                                            "Assenza su più giorni"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isSingleDay) {
                                            "Seleziona l'orario di inizio e fine assenza per questo giorno"
                                        } else {
                                            "Orario di inizio il ${addAbsence.startDate?.format(DateTimeFormatter.ofPattern("dd/MM"))} e fine il ${addAbsence.endDate?.format(DateTimeFormatter.ofPattern("dd/MM"))}. I giorni intermedi saranno calcolati come assenza completa."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time Range Picker
                        if (isSingleDay) {
                            SingleDayTimeRangePicker(
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
                        } else {
                            MultiDayTimeRangePicker(
                                startTime = addAbsence.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                endTime = addAbsence.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                startDate = addAbsence.startDate,
                                endDate = addAbsence.endDate,
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

            // Reason and Comments
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

            // Spacer finale per garantire che l'ultimo elemento sia completamente visibile
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs - mantieni questi fuori dal contenuto scrollabile
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    absenceVM.updateAddAbsenceStartDate(selectedDate)

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

    StatusDialog(
        message = uiState.resultMsg,
        statusType = uiState.statusMsg,
        onDismiss = {
            absenceVM.clearResultMessage()
        }
    )
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


@Composable
fun SingleDayTimeRangePicker(
    startTime: String,
    endTime: String,
    onStartTimeSelected: (String) -> Unit,
    onEndTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Orario di assenza",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePickerField(
                    label = "Dalle ore",
                    time = startTime,
                    onTimeSelected = { time ->
                        // Validazione per giorno singolo: deve essere prima dell'ora di fine
                        if (endTime.isNotEmpty()) {
                            val startTimeObj = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                            val endTimeObj = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))

                            if (startTimeObj.isBefore(endTimeObj)) {
                                onStartTimeSelected(time)
                            }
                        } else {
                            onStartTimeSelected(time)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Freccia di connessione
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                TimePickerField(
                    label = "Alle ore",
                    time = endTime,
                    onTimeSelected = { time ->
                        // Validazione per giorno singolo: deve essere dopo l'ora di inizio
                        if (startTime.isNotEmpty()) {
                            val startTimeObj = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
                            val endTimeObj = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))

                            if (endTimeObj.isAfter(startTimeObj)) {
                                onEndTimeSelected(time)
                            }
                        } else {
                            onEndTimeSelected(time)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Durata calcolata per giorno singolo
            if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                val duration = calculateSingleDayDuration(startTime, endTime)
                if (duration.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ore di assenza: $duration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Nota informativa
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "L'orario di fine deve essere successivo all'orario di inizio nello stesso giorno",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

private fun calculateSingleDayDuration(startTime: String, endTime: String): String {
    return try {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()
        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        // Per giorno singolo, l'ora di fine deve essere dopo quella di inizio
        if (endTotalMinutes > startTotalMinutes) {
            val durationMinutes = endTotalMinutes - startTotalMinutes
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60

            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> ""
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}


@Composable
fun MultiDayTimeRangePicker(
    startTime: String,
    endTime: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartTimeSelected: (String) -> Unit,
    onEndTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Orario di inizio e fine assenza",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Inizio assenza",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TimePickerField(
                        label = "Dalle ore",
                        time = startTime,
                        onTimeSelected = onStartTimeSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Separatore visivo
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Fine assenza",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TimePickerField(
                        label = "Alle ore",
                        time = endTime,
                        onTimeSelected = onEndTimeSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Calcolo giorni intermedi
            if (startDate != null && endDate != null) {
                val intermediateDays = ChronoUnit.DAYS.between(startDate, endDate) - 1
                if (intermediateDays > 0) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (intermediateDays == 1L) {
                                    "1 giorno intermedio (assenza completa)"
                                } else {
                                    "$intermediateDays giorni intermedi (assenza completa)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Nota informativa per multi-day
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Come funziona:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Primo giorno: assenza dall'orario selezionato fino a fine giornata\n" +
                                "• Giorni intermedi: assenza completa (24h)\n" +
                                "• Ultimo giorno: assenza da inizio giornata all'orario selezionato",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}