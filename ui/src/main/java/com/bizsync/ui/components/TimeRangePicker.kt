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
import androidx.compose.ui.unit.dp

@Composable
fun TimeRangePicker(
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
                text = "Orario di lavoro",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePickerField(
                    label = "Inizio",
                    time = startTime,
                    onTimeSelected = onStartTimeSelected,
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
                    label = "Fine",
                    time = endTime,
                    onTimeSelected = onEndTimeSelected,
                    modifier = Modifier.weight(1f)
                )
            }

            // Durata calcolata
            if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                val duration = calculateDuration(startTime, endTime)
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
                                text = "Durata: $duration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun calculateDuration(startTime: String, endTime: String): String {
    return try {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()
        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

        val startTotalMinutes = startHour * 60 + startMinute
        var endTotalMinutes = endHour * 60 + endMinute

        // Gestisce il caso in cui il turno va oltre la mezzanotte
        if (endTotalMinutes <= startTotalMinutes) {
            endTotalMinutes += 24 * 60
        }

        val durationMinutes = endTotalMinutes - startTotalMinutes
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        "${hours}h ${minutes}m"
    } catch (e: Exception) {
        ""
    }
}