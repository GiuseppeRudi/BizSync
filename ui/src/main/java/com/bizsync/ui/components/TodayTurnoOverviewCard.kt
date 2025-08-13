package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.StatoTurno
import com.bizsync.domain.model.TurnoWithDetails
import java.time.format.DateTimeFormatter


@Composable
fun TodayTurnoOverviewCard(
    turnoWithDetails: TurnoWithDetails,
    modifier: Modifier = Modifier
) {
    val statoColor = when (turnoWithDetails.statoTurno) {
        StatoTurno.NON_INIZIATO -> Color(0xFF9E9E9E)
        StatoTurno.IN_CORSO -> Color(0xFF4CAF50)
        StatoTurno.IN_PAUSA -> Color(0xFFFF9800)
        StatoTurno.COMPLETATO -> Color(0xFF2196F3)
        StatoTurno.IN_RITARDO -> Color(0xFFFF5722)
        StatoTurno.ASSENTE -> Color(0xFFF44336)
    }

    val statoText = when (turnoWithDetails.statoTurno) {
        StatoTurno.NON_INIZIATO -> "Non iniziato"
        StatoTurno.IN_CORSO -> "In corso"
        StatoTurno.IN_PAUSA -> "In pausa"
        StatoTurno.COMPLETATO -> "Completato"
        StatoTurno.IN_RITARDO -> "In ritardo"
        StatoTurno.ASSENTE -> "Assente"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turno di Oggi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = turnoWithDetails.turno.titolo,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Badge(
                    containerColor = statoColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statoText,
                        color = statoColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orari
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Orario Previsto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${turnoWithDetails.turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turnoWithDetails.turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (turnoWithDetails.orarioEntrataEffettivo != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Entrata Effettiva",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = turnoWithDetails.orarioEntrataEffettivo!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (turnoWithDetails.minutiRitardo > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Indicatori ritardo/anticipo
            if (turnoWithDetails.minutiRitardo > 0 || turnoWithDetails.minutiAnticipo > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                if (turnoWithDetails.minutiRitardo > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF44336).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⏰ Ritardo: ${turnoWithDetails.minutiRitardo} minuti",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (turnoWithDetails.minutiAnticipo > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⚡ Uscita anticipata: ${turnoWithDetails.minutiAnticipo} minuti",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar dello stato del turno
            val progress = when {
                turnoWithDetails.statoTurno == StatoTurno.COMPLETATO -> 1f
                turnoWithDetails.haTimbratoEntrata -> 0.5f
                else -> 0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statoColor,
                trackColor = statoColor.copy(alpha = 0.2f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}
