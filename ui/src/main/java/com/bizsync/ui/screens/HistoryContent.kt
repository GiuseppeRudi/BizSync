package com.bizsync.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.RequestViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import java.time.format.DateTimeFormatter

@Composable
fun HistoryContent(requestVM: RequestViewModel) {
    val requestState by requestVM.uiState.collectAsState()
    val allRequests = requestState.historyRequests

    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 3 // Prima pagina: 3 elementi
    val additionalItemsPerPage = 4 // Pagine successive: 4 elementi

    // Calcola quanti elementi mostrare
    val itemsToShow = if (currentPage == 0) {
        itemsPerPage
    } else {
        itemsPerPage + (currentPage * additionalItemsPerPage)
    }

    val displayedRequests = allRequests.take(itemsToShow)
    val hasMoreItems = displayedRequests.size < allRequests.size

    if (allRequests.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.History,
            title = "Nessuna cronologia",
            description = "Non ci sono richieste elaborate"
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mostra gli elementi caricati
            displayedRequests.forEach { request ->
                HistoryRequestCard(request = request)
            }

            // Bottone "Carica altri" se ci sono più elementi
            if (hasMoreItems) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        currentPage++
                        Log.d("HISTORY_PAGINATION", "Loading page $currentPage - Showing ${itemsToShow + additionalItemsPerPage} of ${allRequests.size}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val remainingItems = allRequests.size - itemsToShow
                    val itemsToLoad = minOf(additionalItemsPerPage, remainingItems)
                    Text("Carica altri $itemsToLoad elementi ($remainingItems rimanenti)")
                }
            } else if (allRequests.size > itemsPerPage) {
                // Mostra info quando tutti gli elementi sono caricati
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tutti gli elementi caricati (${allRequests.size} totali)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRequestCard(request: AbsenceUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header essenziale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.typeUi.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = request.formattedDateRange,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge stato minimalista
                Surface(
                    color = request.statusUi.color,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = request.statusUi.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Info durata compatta
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = request.formattedTotalDays,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                request.formattedTotalHours?.let { hours ->
                    Text(
                        text = hours,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                request.formattedHours?.let { timeRange ->
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Richiedente e data in una riga
            if (request.submittedName.isNotBlank()) {
                Text(
                    text = "${request.submittedName} • ${request.submittedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Motivo (se presente) - limitato a 2 righe
            if (request.reason.isNotBlank()) {
                Text(
                    text = request.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Info approvazione compatta (solo per stati finali)
            if (request.statusUi.status == AbsenceStatus.APPROVED ||
                request.statusUi.status == AbsenceStatus.REJECTED) {

                val approvalInfo = buildString {
                    request.approver?.let { append("$it • ") }
                    request.approvedDate?.let {
                        append(it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                }

                if (approvalInfo.isNotBlank()) {
                    Text(
                        text = approvalInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = request.statusUi.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Note manager (solo se presenti)
            request.comments?.takeIf { it.isNotBlank() }?.let { comments ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "Note: $comments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}