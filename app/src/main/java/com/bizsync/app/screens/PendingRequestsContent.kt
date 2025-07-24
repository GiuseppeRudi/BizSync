package com.bizsync.app.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.viewmodels.RequestViewModel



enum class DialogType {
    APPROVE, REJECT
}

@Composable
fun PendingRequestsContent(
    requestVM : RequestViewModel,
    approver: String
) {
    val requestState by requestVM.uiState.collectAsState()
    val requests = requestState.pendingRequests

    if (requests.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.CheckCircle,
            title = "Nessuna richiesta in attesa",
            description = "Tutte le richieste sono state gestite"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                PendingRequestCard(
                    request = request,
                    approver,
                    requestVM
                )
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: AbsenceUi,
    approver: String,
    requestVM: RequestViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf<DialogType?>(null) }

    val requestState by requestVM.uiState.collectAsState()

    // Trova il contratto del dipendente che ha fatto la richiesta
    val employeeContract = requestState.contracts.find { it.idDipendente == request.idUser }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = request.submittedName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request.typeUi.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = request.typeUi.color
                    )
                }

                Surface(
                    color = request.statusUi.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = request.statusUi.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = request.statusUi.color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            RequestDetailsSection(request)

            Spacer(modifier = Modifier.height(12.dp))

            employeeContract?.let { contract ->
                ContractImpactSection(
                    request = request,
                    contract = contract
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        dialogType = DialogType.REJECT
                        showDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rifiuta")
                }

                Button(
                    onClick = {
                        dialogType = DialogType.APPROVE
                        showDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approva")
                }
            }
        }
    }

    if (showDialog && dialogType != null) {
        ActionDialog(
            type = dialogType!!,
            request = request,
            contract = employeeContract,
            onDismiss = {
                showDialog = false
                dialogType = null
            },
            onConfirm = { comment ->
                val isApproved = dialogType == DialogType.APPROVE
                requestVM.handleRequestDecision(approver, request, isApproved, comment, employeeContract)
                showDialog = false
                dialogType = null
            }
        )
    }
}
@Composable
private fun ActionDialog(
    type: DialogType,
    request: AbsenceUi,
    contract: Contratto?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    val isApprove = type == DialogType.APPROVE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isApprove) "Approva Richiesta" else "Rifiuta Richiesta",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Riepilogo Richiesta",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dipendente: ${request.submittedName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Tipo: ${request.typeUi.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Periodo: ${request.formattedDateRange}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        val durationText = when (request.typeUi.type) {
                            AbsenceType.ROL -> request.formattedTotalHours ?: "0 ore"
                            else -> request.formattedTotalDays
                        }
                        Text(
                            text = "Durata: $durationText",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isApprove && contract != null) {
                    val impactInfo = calculateContractImpact(request, contract)
                    impactInfo?.let { impact ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (impact.exceedsLimit) {
                                    Color(0xFFFFEBEE)
                                } else {
                                    Color(0xFFE8F5E8)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (impact.exceedsLimit) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (impact.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Impatto Contrattuale",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${impact.type}: ${impact.currentUsed} + ${impact.requestedAmount} = ${impact.totalAfterRequest} / ${impact.maxAllowed} ${impact.unit}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (impact.exceedsLimit) {
                                    Text(
                                        text = "⚠️ ATTENZIONE: Supererà il limite di ${impact.maxAllowed} ${impact.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    val remaining = impact.maxAllowed - impact.totalAfterRequest
                                    Text(
                                        text = "✅ Entro i limiti. Rimarranno $remaining ${impact.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Text(
                    text = if (isApprove)
                        "Vuoi aggiungere una nota all'approvazione?"
                    else
                        "Specifica il motivo del rifiuto:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(comment) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApprove) Color(0xFF4CAF50) else Color.Red
                ),
                enabled = if (isApprove) true else comment.isNotBlank()
            ) {
                Text(if (isApprove) "Conferma Approvazione" else "Conferma Rifiuto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

private fun calculateContractImpact(
    request: AbsenceUi,
    contract: Contratto
): ContractImpact? {
    return when (request.typeUi.type) {
        AbsenceType.VACATION -> {
            val requestedDays = request.totalDays ?: 0
            val currentUsed = contract.ferieUsate
            val maxAllowed = contract.ccnlInfo.ferieAnnue
            val totalAfterRequest = currentUsed + requestedDays
            ContractImpact(
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
            val requestedHours = request.totalHours ?: 0
            val currentUsed = contract.rolUsate
            val maxAllowed = contract.ccnlInfo.rolAnnui
            val totalAfterRequest = currentUsed + requestedHours
            ContractImpact(
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
            val requestedDays = request.totalDays ?: 0
            val currentUsed = contract.malattiaUsata
            val maxAllowed = contract.ccnlInfo.malattiaRetribuita
            val totalAfterRequest = currentUsed + requestedDays
            ContractImpact(
                type = "Malattia",
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
}

@Composable
private fun RequestDetailsSection(request: AbsenceUi) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = request.formattedDateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        request.formattedHours?.let { hours ->
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Total duration
        Spacer(modifier = Modifier.height(4.dp))
        val totalText = when (request.typeUi.type) {
            AbsenceType.ROL -> request.formattedTotalHours ?: "0 ore"
            else -> request.formattedTotalDays
        }
        Text(
            text = "Durata: $totalText",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        // Reason
        if (request.reason.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Motivo: ${request.reason}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ContractImpactSection(
    request: AbsenceUi,
    contract: Contratto
) {
    // Calcola l'impatto in base al tipo di richiesta
    val impactInfo = when (request.typeUi.type) {
        AbsenceType.VACATION -> {
            val requestedDays = request.totalDays ?: 0
            val currentUsed = contract.ferieUsate
            val maxAllowed = contract.ccnlInfo.ferieAnnue
            val totalAfterRequest = currentUsed + requestedDays
            ContractImpact(
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
            val requestedHours = request.totalHours ?: 0
            val currentUsed = contract.rolUsate
            val maxAllowed = contract.ccnlInfo.rolAnnui
            val totalAfterRequest = currentUsed + requestedHours
            ContractImpact(
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
            val requestedDays = request.totalDays ?: 0
            val currentUsed = contract.malattiaUsata
            val maxAllowed = contract.ccnlInfo.malattiaRetribuita
            val totalAfterRequest = currentUsed + requestedDays
            ContractImpact(
                type = "Malattia",
                currentUsed = currentUsed,
                maxAllowed = maxAllowed,
                requestedAmount = requestedDays,
                totalAfterRequest = totalAfterRequest,
                unit = "giorni",
                exceedsLimit = totalAfterRequest > maxAllowed
            )
        }

        else -> null // PERSONAL_LEAVE, UNPAID_LEAVE, STRIKE non hanno limiti
    }

    impactInfo?.let { impact ->
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (impact.exceedsLimit) {
                    Color(0xFFFFEBEE)
                } else {
                    Color(0xFFE8F5E8)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (impact.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (impact.exceedsLimit) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (impact.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Impatto su ${impact.type}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (impact.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Già utilizzate:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "${impact.currentUsed} ${impact.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Questa richiesta:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "+${impact.requestedAmount} ${impact.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Totale dopo approvazione:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${impact.totalAfterRequest} / ${impact.maxAllowed} ${impact.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (impact.exceedsLimit) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                    )
                }

                if (impact.exceedsLimit) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Attenzione: Questa richiesta supererà il limite contrattuale di ${impact.maxAllowed} ${impact.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

data class ContractImpact(
    val type: String,
    val currentUsed: Int,
    val maxAllowed: Int,
    val requestedAmount: Int,
    val totalAfterRequest: Int,
    val unit: String,
    val exceedsLimit: Boolean
)