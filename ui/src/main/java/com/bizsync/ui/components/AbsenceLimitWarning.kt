package com.bizsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.model.AbsenceTypeUi
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit



data class AbsenceLimitInfo(
    val type: String,
    val currentUsed: Int,
    val maxAllowed: Int,
    val requestedAmount: Int,
    val totalAfterRequest: Int,
    val unit: String,
    val exceedsLimit: Boolean
)

@Composable
fun AbsenceLimitWarning(
    selectedType: AbsenceTypeUi?,
    totalDays: Int?,
    totalHours: Int?,
    contratto: Contratto?
) {
    if (selectedType == null || contratto == null) return

    val warningInfo = when (selectedType.type) {
        AbsenceType.VACATION -> {
            val requestedDays = totalDays ?: 0
            val currentUsed = contratto.ferieUsate
            val maxAllowed = contratto.ccnlInfo.ferieAnnue
            val totalAfterRequest = currentUsed + requestedDays

            AbsenceLimitInfo(
                type = "Ferie",
                currentUsed = currentUsed,
                maxAllowed = maxAllowed,
                requestedAmount = requestedDays,
                totalAfterRequest = totalAfterRequest,
                unit = "giorni",
                exceedsLimit = totalAfterRequest > maxAllowed
            )
        }

        AbsenceType.ROL -> {
            val requestedHours = totalHours ?: 0
            val currentUsed = contratto.rolUsate
            val maxAllowed = contratto.ccnlInfo.rolAnnui
            val totalAfterRequest = currentUsed + requestedHours

            AbsenceLimitInfo(
                type = "Permessi ROL",
                currentUsed = currentUsed,
                maxAllowed = maxAllowed,
                requestedAmount = requestedHours,
                totalAfterRequest = totalAfterRequest,
                unit = "ore",
                exceedsLimit = totalAfterRequest > maxAllowed
            )
        }

        AbsenceType.SICK_LEAVE -> {
            val requestedDays = totalDays ?: 0
            val currentUsed = contratto.malattiaUsata
            val maxAllowed = contratto.ccnlInfo.malattiaRetribuita
            val totalAfterRequest = currentUsed + requestedDays

            AbsenceLimitInfo(
                type = "Malattia retribuita",
                currentUsed = currentUsed,
                maxAllowed = maxAllowed,
                requestedAmount = requestedDays,
                totalAfterRequest = totalAfterRequest,
                unit = "giorni",
                exceedsLimit = totalAfterRequest > maxAllowed
            )
        }

        else -> null
    }

    warningInfo?.let { info ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (info.exceedsLimit) {
                    Color(0xFFFFEBEE)
                } else {
                    Color(0xFFE8F5E8)
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (info.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (info.exceedsLimit) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (info.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (info.exceedsLimit) "Attenzione: Limite superato" else "Riepilogo richiesta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (info.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (info.exceedsLimit) {
                        "Questa richiesta supererà il limite annuale per ${info.type}:"
                    } else {
                        "Riepilogo della richiesta per ${info.type}:"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (info.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "• Già utilizzate: ${info.currentUsed} ${info.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "• Richiesta attuale: ${info.requestedAmount} ${info.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "• Totale dopo richiesta: ${info.totalAfterRequest} ${info.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (info.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                    Text(
                        text = "• Limite annuale: ${info.maxAllowed} ${info.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )

                    if (info.exceedsLimit) {
                        Text(
                            text = "• Eccedenza: ${info.totalAfterRequest - info.maxAllowed} ${info.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    } else {
                        Text(
                            text = "• Rimanenti: ${info.maxAllowed - info.totalAfterRequest} ${info.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (info.exceedsLimit) {
                        "⚠️ Puoi comunque inviare la richiesta, ma l'eccedenza potrebbe non essere retribuita o richiedere approvazione speciale."
                    } else {
                        val percentage = if (info.maxAllowed > 0) {
                            ((info.totalAfterRequest.toFloat() / info.maxAllowed.toFloat()) * 100).toInt()
                        } else 0
                        "✅ Richiesta entro i limiti (${percentage}% del limite annuale utilizzato)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = if (info.exceedsLimit) Color(0xFF795548) else Color(0xFF2E7D32)
                )
            }
        }
    }
}

fun calculateRequestedHours(
    totalDays: Int?,
    isFullDay: Boolean,
    startTime: LocalTime?,
    endTime: LocalTime?,
    startDate: LocalDate?,
    endDate: LocalDate?
): Int {
    if (isFullDay) {
        // Se è giornata intera, calcola in base alle ore standard (es. 8 ore/giorno)
        val days = totalDays ?: 0 // ← GESTISCE NULL
        return days * 8
    } else {
        // Se è orario specifico, calcola le ore effettive
        return if (startTime != null && endTime != null) {
            if (startDate == endDate) {
                // Giorno singolo
                val duration = java.time.Duration.between(startTime, endTime)
                duration.toHours().toInt()
            } else {
                // Multi-day - calcolo più complesso
                calculateMultiDayHours(startTime, endTime, startDate, endDate)
            }
        } else {
            0
        }
    }
}

fun calculateMultiDayHours(
    startTime: LocalTime,
    endTime: LocalTime,
    startDate: LocalDate?,
    endDate: LocalDate?
): Int {
    return if (startDate != null && endDate != null) {
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()
        when (daysBetween) {
            0 -> {
                // Stesso giorno
                java.time.Duration.between(startTime, endTime).toHours().toInt()
            }
            1 -> {
                // Due giorni
                val firstDayHours = java.time.Duration.between(startTime, LocalTime.of(17, 0)).toHours()
                val secondDayHours = java.time.Duration.between(LocalTime.of(9, 0), endTime).toHours()
                (firstDayHours + secondDayHours).toInt()
            }
            else -> {
                // Più giorni - calcolo semplificato
                val firstDayHours = java.time.Duration.between(startTime, LocalTime.of(17, 0)).toHours()
                val lastDayHours = java.time.Duration.between(LocalTime.of(9, 0), endTime).toHours()
                val intermediateDays = daysBetween - 1
                (firstDayHours + lastDayHours + (intermediateDays * 8)).toInt()
            }
        }
    } else {
        0
    }
}