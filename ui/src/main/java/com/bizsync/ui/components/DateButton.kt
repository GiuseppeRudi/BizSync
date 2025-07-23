package com.bizsync.ui.components

import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Aggiorna il componente DateButton per supportare lo stato di errore

@Composable
fun DateButton(
    label: String,
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Soluzione 1: Usa Box con clickable
    Box(
        modifier = modifier.clickable { onClick() }
    ) {
        OutlinedTextField(
            value = selectedDate?.format(formatter) ?: "",
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            enabled = false, // 🆕 Disabilita per evitare conflitti
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Seleziona data",
                    tint = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                // Colori per stato disabled che sembrano enabled
                disabledBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                },
                disabledLabelColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ),
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
// Aggiorna anche il DatePickerDialog per supportare date minime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogModify(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate? = null,
    minimumDate: LocalDate? = null // 🆕 Nuovo parametro
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (minimumDate == null) return true

                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                return !date.isBefore(minimumDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return minimumDate?.year?.let { minYear ->
                    year >= minYear
                } ?: true
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("Conferma")
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
                minimumDate?.let { minDate ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    Text(
                        text = "Data minima: ${minDate.format(formatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        )
    }
}

// Aggiorna anche la funzione calculateHoursBetween per gestire casi edge

fun calculateHoursBetween(startTime: LocalTime, endTime: LocalTime): Int {
    return if (endTime.isAfter(startTime)) {
        Duration.between(startTime, endTime).toHours().toInt()
    } else {
        0 // Se l'orario di fine è prima di quello di inizio
    }
}

// 🆕 Aggiungi funzione per formattare meglio i giorni della settimana

fun LocalDate.getDayOfWeekInItalian(): String {
    return when (this.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lunedì"
        DayOfWeek.TUESDAY -> "Martedì"
        DayOfWeek.WEDNESDAY -> "Mercoledì"
        DayOfWeek.THURSDAY -> "Giovedì"
        DayOfWeek.FRIDAY -> "Venerdì"
        DayOfWeek.SATURDAY -> "Sabato"
        DayOfWeek.SUNDAY -> "Domenica"
    }
}

// 🆕 Funzione per calcolare giorni lavorativi tra due date (escludendo weekend)

fun calculateWorkingDaysBetween(startDate: LocalDate, endDate: LocalDate): Int {
    var workingDays = 0
    var currentDate = startDate

    while (!currentDate.isAfter(endDate)) {
        if (currentDate.dayOfWeek != DayOfWeek.SATURDAY &&
            currentDate.dayOfWeek != DayOfWeek.SUNDAY) {
            workingDays++
        }
        currentDate = currentDate.plusDays(1)
    }

    return workingDays
}