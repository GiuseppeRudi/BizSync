package com.bizsync.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parsing del tempo corrente per inizializzare il picker
    val (currentHour, currentMinute) = remember(time) {
        if (time.isNotEmpty() && time.contains(":")) {
            val parts = time.split(":")
            Pair(parts[0].toIntOrNull() ?: 9, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(9, 0)
        }
    }

    val timePickerDialog = remember(currentHour, currentMinute) {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                val formattedTime = String.format("%02d:%02d", hour, minute)
                onTimeSelected(formattedTime)
            },
            currentHour,
            currentMinute,
            true // Formato 24h
        )
    }

    Card(
        modifier = modifier.clickable { timePickerDialog.show() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (time.isNotEmpty()) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = if (time.isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (time.isNotEmpty()) time else "--:--",
                style = MaterialTheme.typography.headlineSmall,
                color = if (time.isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}