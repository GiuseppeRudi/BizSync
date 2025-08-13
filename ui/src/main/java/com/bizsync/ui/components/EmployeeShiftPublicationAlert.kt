package com.bizsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.UrgencyLevel


@Composable
fun EmployeeShiftPublicationAlert(
    daysUntilPublication: Int
) {
    val urgencyLevel = when {
        daysUntilPublication <= 0 -> UrgencyLevel.CRITICAL
        daysUntilPublication == 1 -> UrgencyLevel.HIGH
        daysUntilPublication <= 2 -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }

    val colors = when (urgencyLevel) {
        UrgencyLevel.CRITICAL -> Triple(
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            Icons.Default.Schedule
        )
        UrgencyLevel.HIGH -> Triple(
            Color(0xFFFF9800),
            Color(0xFFFFF3E0),
            Icons.Default.Schedule
        )
        UrgencyLevel.MEDIUM -> Triple(
            Color(0xFFFFC107),
            Color(0xFFFFFDE7),
            Icons.Default.Schedule
        )
        UrgencyLevel.LOW -> Triple(
            Color(0xFF2196F3),
            Color(0xFFE3F2FD),
            Icons.Default.Schedule
        )
    }

    val message = when {
        daysUntilPublication < 0 -> "I turni della prossima settimana sono in ritardo!"
        daysUntilPublication == 0 -> "I turni vengono pubblicati oggi!"
        daysUntilPublication == 1 -> "I turni vengono pubblicati domani"
        else -> "I turni saranno pubblicati tra $daysUntilPublication giorni"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.second
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = colors.third,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.first
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📅 Pubblicazione Turni",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.first
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}