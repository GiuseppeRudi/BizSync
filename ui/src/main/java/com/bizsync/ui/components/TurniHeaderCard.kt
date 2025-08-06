package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.StatisticheSettimanali
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.utils.WeeklyWindowCalculator
import java.time.format.DateTimeFormatter

@Composable
fun TurniHeaderCard(
    statistiche: StatisticheSettimanali?,
    weeklyShift: WeeklyShift?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "I Miei Turni",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Visualizza i tuoi turni assegnati",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (weeklyShift != null )
            {
                val (weekStart, weekEnd) = WeeklyWindowCalculator.getWeekBounds(weeklyShift.weekStart)


                if(statistiche != null)
                {
                    // Prima riga: Ore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ore assegnate: ${statistiche.oreAssegnate}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Ore contrattuali: ${statistiche.oreContrattuali}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(4.dp))

                // Seconda riga: Periodo settimana
                Text(
                    text = "Settimana: ${weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Terza riga: Status e data creazione
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status con colore
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val (statusText, statusColor) = when (weeklyShift.status) {
                            WeeklyShiftStatus.PUBLISHED -> "Pubblicata" to Color(0xFF4CAF50)
                            WeeklyShiftStatus.DRAFT -> "Bozza" to Color(0xFFFF9800)
                            WeeklyShiftStatus.NOT_PUBLISHED -> "In lavorazione" to Color(0xFF9E9E9E)
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "Creata: ${weeklyShift.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
