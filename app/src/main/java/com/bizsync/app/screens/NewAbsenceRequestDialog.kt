package com.bizsync.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.constants.enumClass.AbsenceTimeType
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.ui.components.AbsenceLimitWarning
import com.bizsync.ui.components.AbsenceTypeSelector
import com.bizsync.ui.components.DateButton
import com.bizsync.ui.components.DatePickerDialog
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.SingleDayTimeRangePicker
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.components.calculateRequestedHours
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.AbsenceViewModel
import com.bizsync.ui.viewmodels.UserViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// Funzione helper per calcolare ore
fun calculateHoursBetween(startTime: LocalTime, endTime: LocalTime): Int {
    return Duration.between(startTime, endTime).toHours().toInt()
}

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
    val contratto = userState.contratto

    LaunchedEffect(status) {
        if (status == DialogStatusType.SUCCESS) {
            scaffoldVM.onFullScreenChanged(true)
            onSubmit()
        }
    }

//    LaunchedEffect(uiState.contract) {
//        val contract = uiState.contract // ← Copia in variabile locale
//        if (contract != null) {
//            userVM.changeContract(contract) // ← Ora può fare smart cast
//        }
//    }

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }

    LaunchedEffect(
        addAbsence.startDate,
        addAbsence.endDate,
        addAbsence.startTime,
        addAbsence.endTime,
        uiState.selectedTimeType,
        uiState.isFlexibleModeFullDay,
        addAbsence.typeUi
    ) {
        val startDate = addAbsence.startDate
        val endDate = addAbsence.endDate
        val startTime = addAbsence.startTime
        val endTime = addAbsence.endTime

        when (uiState.selectedTimeType) {
            AbsenceTimeType.FULL_DAYS_ONLY -> {
                // Solo calcolo giorni per VACATION, SICK_LEAVE, STRIKE
                if (startDate != null && endDate != null) {
                    val totalDays = absenceVM.calculateTotalDaysInt(startDate, endDate)
                    absenceVM.updateAddAbsenceTotalDays(totalDays)
                    absenceVM.updateAddAbsenceTotalHours(null) // Reset ore
                } else {
                    absenceVM.updateAddAbsenceTotalDays(0)
                    absenceVM.updateAddAbsenceTotalHours(null)
                }
            }

            AbsenceTimeType.HOURLY_SINGLE_DAY -> {
                // Solo calcolo ore per ROL
                if (startTime != null && endTime != null && startDate != null) {
                    val hours = calculateHoursBetween(startTime, endTime)
                    absenceVM.updateAddAbsenceTotalHours(hours)
                    absenceVM.updateAddAbsenceTotalDays(0) // ROL non usa giorni
                } else {
                    absenceVM.updateAddAbsenceTotalHours(0)
                    absenceVM.updateAddAbsenceTotalDays(0)
                }
            }

            AbsenceTimeType.FLEXIBLE -> {
                if (uiState.isFlexibleModeFullDay) {
                    // Modalità giorni interi per PERSONAL_LEAVE/UNPAID_LEAVE
                    if (startDate != null && endDate != null) {
                        val totalDays = absenceVM.calculateTotalDaysInt(startDate, endDate)
                        absenceVM.updateAddAbsenceTotalDays(totalDays)
                        absenceVM.updateAddAbsenceTotalHours(null)
                    } else {
                        absenceVM.updateAddAbsenceTotalDays(0)
                        absenceVM.updateAddAbsenceTotalHours(null)
                    }
                } else {
                    // Modalità oraria per PERSONAL_LEAVE/UNPAID_LEAVE
                    if (startTime != null && endTime != null && startDate != null) {
                        val hours = calculateHoursBetween(startTime, endTime)
                        absenceVM.updateAddAbsenceTotalHours(hours)
                        absenceVM.updateAddAbsenceTotalDays(0) // Non usare giorni in modalità oraria
                    } else {
                        absenceVM.updateAddAbsenceTotalHours(0)
                        absenceVM.updateAddAbsenceTotalDays(0)
                    }
                }
            }
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
                            absenceVM.saveAbsence(fullName,idAzienda, idUser, contratto)
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
            AbsenceTypeSelector(
                selectedType = addAbsence.typeUi,
                onTypeSelected = { selectedType ->
                    selectedType?.let { absenceVM.updateAddAbsenceType(it) }
                }
            )

            // Sostituisci la Card esistente con questo
            AbsencePeriodSelector(
                addAbsence = addAbsence,
                absenceVM = absenceVM,
                timeType = uiState.selectedTimeType,
                isFlexibleModeFullDay = uiState.isFlexibleModeFullDay
            )

            // Controllo limiti per ferie, ROL e malattia
            val requestedHours = if (addAbsence.typeUi.type == AbsenceType.ROL) {
                calculateRequestedHours(
                    addAbsence.totalDays,
                    isFullDay,
                    addAbsence.startTime,
                    addAbsence.endTime,
                    addAbsence.startDate,
                    addAbsence.endDate
                )
            } else null

            AbsenceLimitWarning(
                selectedType = addAbsence.typeUi,
                totalDays = addAbsence.totalDays,
                totalHours = addAbsence.totalHours,
                contratto = contratto
            )

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











// Componente principale per la selezione periodo - sostituisce la Card esistente
@Composable
fun AbsencePeriodSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    timeType: AbsenceTimeType,
    isFlexibleModeFullDay: Boolean
) {
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
                text = when (timeType) {
                    AbsenceTimeType.FULL_DAYS_ONLY -> "Periodo di assenza (giorni interi)"
                    AbsenceTimeType.HOURLY_SINGLE_DAY -> "Giorno e orario di assenza"
                    AbsenceTimeType.FLEXIBLE -> "Periodo di assenza"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            when (timeType) {
                AbsenceTimeType.FULL_DAYS_ONLY -> {
                    FullDaysSelector(addAbsence, absenceVM)
                }
                AbsenceTimeType.HOURLY_SINGLE_DAY -> {
                    SingleDayHourlySelector(addAbsence, absenceVM)
                }
                AbsenceTimeType.FLEXIBLE -> {
                    FlexibleSelector(addAbsence, absenceVM, isFlexibleModeFullDay)
                }
            }

            // Mostra il totale calcolato
// Mostra il totale calcolato
            if ((addAbsence.totalDays ?: 0) > 0 || (addAbsence.totalHours ?: 0) > 0) {
                TotalDisplay(addAbsence, timeType, isFlexibleModeFullDay)
            }
        }
    }
}

// Selettore per giorni interi (VACATION, SICK_LEAVE, STRIKE)
@Composable
private fun FullDaysSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateButton(
                label = "Data inizio",
                selectedDate = addAbsence.startDate,
                modifier = Modifier.weight(1f),
                onClick = { absenceVM.setShowStartDatePicker(true) }
            )

            DateButton(
                label = "Data fine",
                selectedDate = addAbsence.endDate,
                modifier = Modifier.weight(1f),
                onClick = { absenceVM.setShowEndDatePicker(true) }
            )
        }

        // Info card
        InfoCard(
            title = "Giorni interi",
            description = "L'assenza sarà calcolata per giorni lavorativi completi dal ${addAbsence.startDate?.format(DateTimeFormatter.ofPattern("dd/MM")) ?: "..."} al ${addAbsence.endDate?.format(DateTimeFormatter.ofPattern("dd/MM")) ?: "..."}"
        )
    }
}

// Selettore per singolo giorno con orario (ROL)
@Composable
private fun SingleDayHourlySelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DateButton(
            label = "Giorno di assenza",
            selectedDate = addAbsence.startDate,
            modifier = Modifier.fillMaxWidth(),
            onClick = { absenceVM.setShowStartDatePicker(true) }
        )

        if (addAbsence.startDate != null) {
            // Assicurati che endDate sia uguale a startDate per ROL
            LaunchedEffect(addAbsence.startDate) {
                absenceVM.updateAddAbsenceEndDate(addAbsence.startDate!!)
            }

            SingleDayTimeRangePicker(
                startTime = addAbsence.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                endTime = addAbsence.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                onStartTimeSelected = { timeString ->
                    val localTime = try {
                        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) { null }
                    absenceVM.updateAddAbsenceStartTime(localTime)
                },
                onEndTimeSelected = { timeString ->
                    val localTime = try {
                        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) { null }
                    absenceVM.updateAddAbsenceEndTime(localTime)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        InfoCard(
            title = "Permesso orario",
            description = "Seleziona il giorno e la fascia oraria per il permesso ROL"
        )
    }
}

// Selettore flessibile (PERSONAL_LEAVE, UNPAID_LEAVE)
@Composable
private fun FlexibleSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    isFlexibleModeFullDay: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Toggle per scegliere modalità
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tipo di assenza",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    onClick = {
                        absenceVM.setFlexibleModeFullDay(true)
                        // Reset orari quando si passa a giorni interi
                        absenceVM.updateAddAbsenceStartTime(null)
                        absenceVM.updateAddAbsenceEndTime(null)
                    },
                    label = { Text("Giorni interi") },
                    selected = isFlexibleModeFullDay,
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    onClick = {
                        absenceVM.setFlexibleModeFullDay(false)
                        // Assicurati che sia un giorno singolo quando si passa a orario
                        if (addAbsence.startDate != null) {
                            absenceVM.updateAddAbsenceEndDate(addAbsence.startDate!!)
                        }
                    },
                    label = { Text("Fascia oraria") },
                    selected = !isFlexibleModeFullDay,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // Contenuto basato sulla modalità selezionata
        if (isFlexibleModeFullDay) {
            FullDaysSelector(addAbsence, absenceVM)
        } else {
            SingleDayHourlySelector(addAbsence, absenceVM)
        }
    }
}
// Aggiorna il TotalDisplay component
@Composable
private fun TotalDisplay(
    addAbsence: AbsenceUi,
    timeType: AbsenceTimeType,
    isFlexibleModeFullDay: Boolean
) {
    // Mostra solo se c'è qualcosa da mostrare
    val hasValue = when {
        timeType == AbsenceTimeType.HOURLY_SINGLE_DAY -> (addAbsence.totalHours ?: 0) > 0
        timeType == AbsenceTimeType.FLEXIBLE && !isFlexibleModeFullDay -> (addAbsence.totalHours ?: 0) > 0
        else -> (addAbsence.totalDays ?: 0) > 0
    }

    if (hasValue) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                val displayText = when {
                    timeType == AbsenceTimeType.HOURLY_SINGLE_DAY -> {
                        addAbsence.formattedTotalHours ?: "0 ore"
                    }
                    timeType == AbsenceTimeType.FLEXIBLE && !isFlexibleModeFullDay -> {
                        addAbsence.formattedTotalHours ?: "0 ore"
                    }
                    else -> {
                        addAbsence.formattedTotalDays
                    }
                }

                Text(
                    text = "Totale: $displayText",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// Componente info card helper
@Composable
private fun InfoCard(title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}