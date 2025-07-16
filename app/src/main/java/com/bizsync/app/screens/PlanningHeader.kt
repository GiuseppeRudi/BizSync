package com.bizsync.app.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.WeeklyShift
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningHeader(
    weeklyShift: WeeklyShift?,
    hasUnsavedChanges: Boolean = false,
    isLoading: Boolean = false,
    onSync: () -> Unit,
    onStatoSettimana: (WeeklyShiftStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) } // Stato per mostrare/nascondere

    if (weeklyShift != null && weeklyShift.status != WeeklyShiftStatus.PUBLISHED) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header con informazioni settimana e toggle button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pianificazione Turni",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        val weekEnd = weeklyShift.weekStart.plusDays(6)
                        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ITALIAN)

                        Text(
                            text = "${weeklyShift.weekStart.format(formatter)} - ${weekEnd.format(formatter)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Pulsante toggle per nascondere/mostrare
                    Surface(
                        onClick = { isExpanded = !isExpanded },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Nascondi pannello" else "Mostra pannello",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Contenuto espandibile con animazione
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = tween(300, easing = EaseOutCubic)
                    ) + fadeIn(
                        animationSpec = tween(300, easing = EaseOutCubic)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(300, easing = EaseInCubic)
                    ) + fadeOut(
                        animationSpec = tween(300, easing = EaseInCubic)
                    )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Pulsanti di azione
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Pulsante Sincronizza
                            OutlinedButton(
                                onClick = onSync,
                                enabled = !isLoading && hasUnsavedChanges,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasUnsavedChanges) "Sincronizza" else "Sincronizzato",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Status Chip
                            StatusChip(
                                status = weeklyShift.status,
                                onClick = { showStatusDialog = true }
                            )
                        }

                        // Indicatore modifiche non salvate
                        if (hasUnsavedChanges) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.warningContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onWarningContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hai modifiche non sincronizzate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onWarningContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog per cambio stato
    if (showStatusDialog && weeklyShift != null) {
        StatoSettimanaDialog(
            statoCorrente = weeklyShift.status,
            onDismiss = { showStatusDialog = false },
            onStatusChanged = { nuovoStato ->
                onStatoSettimana(nuovoStato)
                showStatusDialog = false
            }
        )
    }
}

@Composable
fun StatusChip(
    status: WeeklyShiftStatus?,
    onClick: () -> Unit
) {
    if (status == null) return

    val (text, color, icon) = when (status) {
        WeeklyShiftStatus.NOT_PUBLISHED -> Triple(
            "Non Pubblicata",
            MaterialTheme.colorScheme.outline,
            Icons.Default.Visibility
        )
        WeeklyShiftStatus.DRAFT -> Triple(
            "Bozza",
            MaterialTheme.colorScheme.primary,
            Icons.Default.Edit
        )
        WeeklyShiftStatus.PUBLISHED -> Triple(
            "Pubblicata",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.PublishedWithChanges
        )
    }

    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = color,
            labelColor = color
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatoSettimanaDialog(
    statoCorrente: WeeklyShiftStatus,
    onDismiss: () -> Unit,
    onStatusChanged: (WeeklyShiftStatus) -> Unit
) {
    var statoSelezionato by remember { mutableStateOf(statoCorrente) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Cambia Stato Settimana")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Scegli lo stato per questa settimana di turni:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(WeeklyShiftStatus.values()) { status ->
                    StatoOption(
                        status = status,
                        isSelected = statoSelezionato == status,
                        isCurrentStatus = status == statoCorrente,
                        onClick = { statoSelezionato = status }
                    )
                }


            }
        },
        confirmButton = {
            Button(
                onClick = { onStatusChanged(statoSelezionato) },
                enabled = statoSelezionato != statoCorrente
            ) {
                Text("Cambia Stato")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun StatoOption(
    status: WeeklyShiftStatus,
    isSelected: Boolean,
    isCurrentStatus: Boolean,
    onClick: () -> Unit
) {
    val (title, subtitle, icon, color) = when (status) {
        WeeklyShiftStatus.NOT_PUBLISHED -> {
            val desc = "I turni rimangono visibili solo a te, anche se sincronizzati. Puoi continuare a modificarli liberamente."
            Quadruple("Non Pubblicata", desc, Icons.Default.Visibility, MaterialTheme.colorScheme.outline)
        }
        WeeklyShiftStatus.DRAFT -> {
            val desc = "I turni vengono sincronizzati e mostrati ai dipendenti come bozza per raccogliere feedback. Puoi ancora modificarli."
            Quadruple("Bozza", desc, Icons.Default.Edit, MaterialTheme.colorScheme.primary)
        }
        WeeklyShiftStatus.PUBLISHED -> {
            val desc = "I turni vengono sincronizzati e pubblicati ufficialmente. I dipendenti li vedono e NON potrai più modificarli."
            Quadruple("Pubblicata", desc, Icons.Default.PublishedWithChanges, MaterialTheme.colorScheme.tertiary)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCurrentStatus -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (isCurrentStatus) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    "Attuale",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Funzioni helper
fun getStatusIcon(status: WeeklyShiftStatus?): ImageVector {
    return when (status) {
        WeeklyShiftStatus.NOT_PUBLISHED -> Icons.Default.Visibility
        WeeklyShiftStatus.DRAFT -> Icons.Default.Edit
        WeeklyShiftStatus.PUBLISHED -> Icons.Default.PublishedWithChanges
        null -> Icons.Default.Settings
    }
}

fun getStatusDescription(status: WeeklyShiftStatus): String {
    return when (status) {
        WeeklyShiftStatus.NOT_PUBLISHED -> {
            "• I turni rimangono privati e visibili solo a te\n" +
                    "• Puoi continuare a modificarli liberamente\n" +
                    "• La sincronizzazione salva le modifiche nel cloud ma non le condivide\n" +
                    "• Ideale per la fase di pianificazione iniziale"
        }
        WeeklyShiftStatus.DRAFT -> {
            "• I turni vengono automaticamente sincronizzati\n" +
                    "• Diventano visibili ai dipendenti come \"bozza\"\n" +
                    "• I dipendenti possono vedere e commentare i turni\n" +
                    "• Puoi ancora modificarli dopo aver raccolto feedback\n" +
                    "• Ideale per raccogliere opinioni prima della pubblicazione finale"
        }
        WeeklyShiftStatus.PUBLISHED -> {
            "• I turni vengono automaticamente sincronizzati\n" +
                    "• Diventano ufficiali e visibili a tutti i dipendenti\n" +
                    "• NON potrai più modificarli una volta pubblicati\n" +
                    "• I dipendenti riceveranno notifiche sui loro turni\n" +
                    "• Questo è il passaggio finale e irreversibile"
        }
    }
}

// Data class helper per tuple a 4 elementi
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// Estensione per i colori di warning
val ColorScheme.warningContainer: androidx.compose.ui.graphics.Color
    get() = primary.copy(alpha = 0.1f)

val ColorScheme.onWarningContainer: androidx.compose.ui.graphics.Color
    get() = primary