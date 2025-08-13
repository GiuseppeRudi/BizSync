package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnhancedStatisticProgressRow(
    label: String,
    approved: Int,
    pending: Int,
    max: Int,
    unit: String,
    color: Color,
    icon: ImageVector
) {
    val total = approved + pending

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$approved",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (pending > 0) {
                    Text(
                        " + $pending",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800)
                    )
                }
                Text(
                    " / $max $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ✅ PROGRESS BAR CORRETTA con segmenti separati
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            val approvedProgress = if (max > 0) (approved.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
            val pendingProgress = if (max > 0) (pending.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
            val totalProgress = (approvedProgress + pendingProgress).coerceIn(0f, 1f)

            // ✅ PRIMO SEGMENTO: Approvate (verde)
            if (approvedProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(approvedProgress)
                        .height(8.dp)
                        .background(
                            color,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            // ✅ SECONDO SEGMENTO: In attesa (arancione) - INIZIA DOPO quello verde
            if (pending > 0 && approvedProgress < 1f) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Spazio per la parte approvata
                    Spacer(modifier = Modifier.fillMaxWidth(approvedProgress))

                    // Parte in attesa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pendingProgress / (1f - approvedProgress))
                            .height(8.dp)
                            .background(
                                if (totalProgress > 1f) Color(0xFFD32F2F) else Color(0xFFFF9800),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dettagli con legenda
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Prima riga: percentuali e rimanenti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val approvedPercentage = if (max > 0) ((approved.toFloat() / max.toFloat()) * 100).toInt() else 0
                val totalPercentage = if (max > 0) ((total.toFloat() / max.toFloat()) * 100).toInt() else 0
                val remaining = (max - total).coerceAtLeast(0)

                Text(
                    "Utilizzato: $approvedPercentage%${if (pending > 0) " (+${totalPercentage - approvedPercentage}% in attesa)" else ""}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Text(
                    "Rimangono: $remaining $unit",
                    fontSize = 11.sp,
                    color = when {
                        remaining <= 0 -> Color(0xFFD32F2F)
                        remaining <= max * 0.2 -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                )
            }

            if (pending > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Approvate: $approved $unit",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF9800), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "In attesa: $pending $unit",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}