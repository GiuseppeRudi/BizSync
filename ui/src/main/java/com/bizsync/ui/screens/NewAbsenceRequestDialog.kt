package com.bizsync.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
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
import androidx.compose.material3.Surface
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
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.domain.constants.enumClass.AbsenceTimeType
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.ui.components.AbsenceLimitWarning
import com.bizsync.ui.components.AbsenceTypeSelector
import com.bizsync.ui.components.DateButton
import com.bizsync.ui.components.DatePickerDialogModify
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.SingleDayTimeRangePicker
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.components.calculateRequestedHours
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.AbsenceViewModel
import com.bizsync.ui.viewmodels.UserViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale


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

    val isFullDay = uiState.isFullDay
    val showStartDatePicker = uiState.showStartDatePicker
    val showEndDatePicker = uiState.showEndDatePicker

    val minimumDate = remember(addAbsence.typeUi.type) {
        calculateMinimumAbsenceDate(addAbsence.typeUi.type)
    }

    val status = uiState.statusMsg
    val userState by userVM.uiState.collectAsState()
    val idAzienda = userState.azienda.idAzienda
    val fullName = userState.user.cognome + " " + userState.user.nome
    val idUser = userState.user.uid
    val contratto = userState.contratto

    LaunchedEffect(status) {
        if (status == DialogStatusType.SUCCESS) {
            scaffoldVM.onFullScreenChanged(false)
            onSubmit()
        }
    }


    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
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



    // Aggiorna la validazione per includere la validazione delle date
    val isValidSubmission = remember(
        addAbsence.startDate,
        addAbsence.endDate,
        addAbsence.reason,
        isFullDay,
        addAbsence.startTime,
        addAbsence.endTime,
        minimumDate
    ) {
        val hasValidDates = addAbsence.startDate != null && addAbsence.endDate != null
        val hasValidReason = addAbsence.reason.isNotEmpty()

        val isDateValid = addAbsence.startDate?.let {
            isValidAbsenceDate(it, addAbsence.typeUi.type)
        } ?: false
        val isEndDateValid = addAbsence.endDate?.let {
            isValidAbsenceDate(it, addAbsence.typeUi.type)
        } ?: false


        if (!hasValidDates || !hasValidReason || !isDateValid || !isEndDateValid) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            // Mostra AnticipationWarningCard solo se non è malattia
            if (addAbsence.typeUi.type != AbsenceType.SICK_LEAVE) {
                AnticipationWarningCard(minimumDate = minimumDate)
            } else {
                // Card informativa per malattia
                SickLeaveInfoCard()
            }

            AbsenceTypeSelector(
                selectedType = addAbsence.typeUi,
                onTypeSelected = { selectedType ->
                    selectedType.let { absenceVM.updateAddAbsenceType(it) }
                }
            )

            // Period Selector con validazione date
            AbsencePeriodSelector(
                addAbsence = addAbsence,
                absenceVM = absenceVM,
                timeType = uiState.selectedTimeType,
                isFlexibleModeFullDay = uiState.isFlexibleModeFullDay,
                minimumDate = minimumDate // 🆕 Passa la data minima
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showStartDatePicker) {
        DatePickerDialogModify(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    if (isValidAbsenceDate(selectedDate, addAbsence.typeUi.type)) {
                        absenceVM.updateAddAbsenceStartDate(selectedDate)

                        if (addAbsence.endDate != null && addAbsence.endDate!!.isBefore(selectedDate)) {
                            absenceVM.updateAddAbsenceEndDate(selectedDate)
                        }
                    }
                }
                absenceVM.setShowStartDatePicker(false)
            },
            onDismiss = {
                absenceVM.setShowStartDatePicker(false)
            },
            initialDate = addAbsence.startDate,
            minimumDate = minimumDate
        )
    }

    if (showEndDatePicker) {
        DatePickerDialogModify(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    if (isValidAbsenceDate(selectedDate, addAbsence.typeUi.type) &&
                        (addAbsence.startDate == null || !selectedDate.isBefore(addAbsence.startDate))) {
                        absenceVM.updateAddAbsenceEndDate(selectedDate)
                    }
                }
                absenceVM.setShowEndDatePicker(false)
            },
            onDismiss = {
                absenceVM.setShowEndDatePicker(false)
            },
            initialDate = addAbsence.endDate ?: addAbsence.startDate,
            minimumDate = minimumDate
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
fun SickLeaveInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Richiesta di Malattia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Le richieste di malattia vengono approvate automaticamente e possono essere inviate anche per il giorno stesso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = "⚠️ Ricorda di fornire il certificato medico appena disponibile",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
@Composable
fun AnticipationWarningCard(minimumDate: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ITALIAN)
    val oggi = LocalDate.now()
    val giorniMancanti = ChronoUnit.DAYS.between(oggi, minimumDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Anticipo Richiesto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Text(
                text = "Le richieste di assenza devono essere inviate con almeno 2 settimane di anticipo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prima data disponibile:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = minimumDate.format(formatter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "$giorniMancanti giorni",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AbsencePeriodSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    timeType: AbsenceTimeType,
    isFlexibleModeFullDay: Boolean,
    minimumDate: LocalDate
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
                    FullDaysSelector(addAbsence, absenceVM, minimumDate)
                }
                AbsenceTimeType.HOURLY_SINGLE_DAY -> {
                    SingleDayHourlySelector(addAbsence, absenceVM, minimumDate)
                }
                AbsenceTimeType.FLEXIBLE -> {
                    FlexibleSelector(addAbsence, absenceVM, isFlexibleModeFullDay, minimumDate)
                }
            }

            // ✅ CORRETTO: Passa anche il tipo di assenza
            if (addAbsence.startDate != null && !isValidAbsenceDate(addAbsence.startDate!!, addAbsence.typeUi.type)) {
                DateValidationError(minimumDate)
            }

            if ((addAbsence.totalDays ?: 0) > 0 || (addAbsence.totalHours ?: 0) > 0) {
                TotalDisplay(addAbsence, timeType, isFlexibleModeFullDay)
            }
        }
    }
}


@Composable
private fun DateValidationError(minimumDate: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Data non valida",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Seleziona una data dal ${minimumDate.format(formatter)} in poi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


@Composable
private fun FullDaysSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    minimumDate: LocalDate
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateButton(
                label = "Inizio",
                selectedDate = addAbsence.startDate,
                modifier = Modifier.weight(1f),
                onClick = { absenceVM.setShowStartDatePicker(true) },
                // ✅ CORRETTO: Passa anche il tipo di assenza
                isError = addAbsence.startDate != null &&
                        !isValidAbsenceDate(addAbsence.startDate!!, addAbsence.typeUi.type)
            )

            DateButton(
                label = "Fine",
                selectedDate = addAbsence.endDate,
                modifier = Modifier.weight(1f),
                onClick = { absenceVM.setShowEndDatePicker(true) },
                // ✅ CORRETTO: Passa anche il tipo di assenza
                isError = addAbsence.endDate != null &&
                        !isValidAbsenceDate(addAbsence.endDate!!, addAbsence.typeUi.type)
            )
        }

        InfoCard(
            title = "Giorni interi",
            description = if (addAbsence.startDate != null && addAbsence.endDate != null) {
                "L'assenza sarà calcolata per giorni lavorativi completi dal ${addAbsence.startDate!!.format(DateTimeFormatter.ofPattern("dd/MM"))} al ${addAbsence.endDate!!.format(DateTimeFormatter.ofPattern("dd/MM"))}"
            } else {
                "Seleziona il periodo di assenza per giorni interi"
            }
        )
    }
}
@Composable
private fun SingleDayHourlySelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    minimumDate: LocalDate
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DateButton(
            label = "Giorno di assenza",
            selectedDate = addAbsence.startDate,
            modifier = Modifier.fillMaxWidth(),
            onClick = { absenceVM.setShowStartDatePicker(true) },
            // ✅ CORRETTO: Passa anche il tipo di assenza
            isError = addAbsence.startDate != null &&
                    !isValidAbsenceDate(addAbsence.startDate!!, addAbsence.typeUi.type)
        )

        if (addAbsence.startDate != null) {
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

@Composable
private fun FlexibleSelector(
    addAbsence: AbsenceUi,
    absenceVM: AbsenceViewModel,
    isFlexibleModeFullDay: Boolean,
    minimumDate: LocalDate
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    onClick = {
                        absenceVM.setFlexibleModeFullDay(true)
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

        if (isFlexibleModeFullDay) {
            FullDaysSelector(addAbsence, absenceVM, minimumDate)
        } else {
            SingleDayHourlySelector(addAbsence, absenceVM, minimumDate)
        }
    }


@Composable
private fun TotalDisplay(
    addAbsence: AbsenceUi,
    timeType: AbsenceTimeType,
    isFlexibleModeFullDay: Boolean
) {

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


/**
 * Calcola la data minima selezionabile per le richieste di assenza
 * Le richieste devono essere inviate con 2 settimane di anticipo (ESCLUSE LE MALATTIE)
 */
fun calculateMinimumAbsenceDate(absenceType: AbsenceType?): LocalDate {
    // Per malattia, permetti data odierna
    if (absenceType == AbsenceType.SICK_LEAVE) {
        return LocalDate.now()
    }

    val oggi = LocalDate.now()
    val lunediSettimanaCorrente = oggi.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return lunediSettimanaCorrente.plusWeeks(2)
}

/**
 * Verifica se una data è valida per una richiesta di assenza
 */
fun isValidAbsenceDate(date: LocalDate, absenceType: AbsenceType?): Boolean {
    return !date.isBefore(calculateMinimumAbsenceDate(absenceType))
}