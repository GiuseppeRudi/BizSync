package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.ui.model.AbsenceStatusUi
import com.bizsync.ui.model.AbsenceUi
import java.time.format.DateTimeFormatter


@Composable
fun AbsenceRequestCard(request: AbsenceUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        request.typeUi.icon,
                        contentDescription = null,
                        tint = request.typeUi.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        request.typeUi.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                StatusChip(request.statusUi)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date range
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = request.formattedDateRange)
            }

            // Hours se presenti
            request.formattedHours?.takeIf { it != "00:00 - 00:00" }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = it)
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Total days/hours
            val totalText = when (request.typeUi.type) {
                AbsenceType.ROL -> {
                    request.formattedTotalHours ?: "0 ore"
                }
                else -> {
                    request.formattedTotalDays
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Totale: $totalText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (request.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Motivo: ${request.reason}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Approval info
            if (request.statusUi.status == AbsenceStatus.APPROVED && request.approver != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Approvata da: ${request.approver} il ${request.approvedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: AbsenceStatusUi) {
    Box(
        modifier = Modifier
            .background(
                status.color.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                status.color.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            status.displayName,
            color = status.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}



