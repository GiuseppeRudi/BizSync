package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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