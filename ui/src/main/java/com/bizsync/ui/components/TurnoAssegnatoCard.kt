package com.bizsync.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.constants.enumClass.TipoPausa
import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.screens.InfoRow
import java.time.format.DateTimeFormatter

@Composable
fun TurnoAssegnatoCard(
    turno: Turno,
    dipendenti: List<User>,
    isIdentical: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Informazioni principali
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = turno.titolo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Orario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dipendenti (primi 3 nomi)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )

                        val dipendentiNomi = dipendenti.map {
                            if (it.nome.isNotBlank() && it.cognome.isNotBlank()) {
                                "${it.nome} ${it.cognome}"
                            } else {
                                it.email.substringBefore("@")
                            }
                        }

                        Text(
                            text = when {
                                dipendentiNomi.isEmpty() -> "Nessun dipendente"
                                dipendentiNomi.size <= 2 -> dipendentiNomi.joinToString(", ")
                                else -> "${dipendentiNomi.take(2).joinToString(", ")} +${dipendentiNomi.size - 2}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Pulsanti modifica/elimina solo se isIdentical
                    if (isIdentical) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Modifica",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Contenuto espandibile
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(300)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Informazioni dettagliate
                    DetailedTurnoInfo(
                        turno = turno,
                        dipendenti = dipendenti
                    )
                }
            }
        }
    }
}


@Composable
private fun DetailedTurnoInfo(
    turno: Turno,
    dipendenti: List<User>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sezione durata e statistiche
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip(
                icon = Icons.Default.Timer,
                label = "Durata",
                value = "${turno.calcolaDurata()}h",
                color = MaterialTheme.colorScheme.primary
            )

            InfoChip(
                icon = Icons.Default.Coffee,
                label = "Pause",
                value = "${turno.pause.size}",
                color = MaterialTheme.colorScheme.secondary
            )

            val durataPause = turno.pause.sumOf { it.durata.toMinutes() }
            InfoChip(
                icon = Icons.Default.AccessTime,
                label = "Pause min",
                value = "${durataPause}min",
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        // Lista dipendenti completa
        if (dipendenti.isNotEmpty()) {
            DetailSection(
                title = "Dipendenti Assegnati",
                icon = Icons.Default.Group
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dipendenti) { dipendente ->
                        DipendenteChip(dipendente = dipendente)
                    }
                }
            }
        }

        // Pause se presenti
        if (turno.pause.isNotEmpty()) {
            DetailSection(
                title = "Pause Configurate",
                icon = Icons.Default.Coffee
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(turno.pause) { pausa ->
                        PausaChip(pausa = pausa)
                    }
                }
            }
        }

        // Note se presenti
        if (turno.note.isNotEmpty()) {
            DetailSection(
                title = "Note",
                icon = Icons.AutoMirrored.Filled.Notes
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    turno.note.forEach { nota ->
                        NotaItem(nota = nota)
                    }
                }
            }
        }


        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(
                label = "Data creazione",
                value = turno.createdAt.toString(),
                icon = Icons.Default.CalendarToday
            )

            if (turno.updatedAt != turno.createdAt) {
                InfoRow(
                    label = "Ultima modifica",
                    value = turno.updatedAt.toString(),
                    icon = Icons.Default.Update
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DipendenteChip(dipendente: User) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = if (dipendente.nome.isNotBlank() && dipendente.cognome.isNotBlank()) {
                    "${dipendente.nome} ${dipendente.cognome}"
                } else {
                    dipendente.email.substringBefore("@")
                },
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun PausaChip(pausa: Pausa) {
    val tipoPausaText = when (pausa.tipo) {
        TipoPausa.PAUSA_PRANZO -> "Pranzo"
        TipoPausa.PAUSA_CAFFE -> "Caffè"
        TipoPausa.RIPOSO_BREVE -> "Riposo"
        TipoPausa.TECNICA -> "Tecnica"
        TipoPausa.OBBLIGATORIA -> "Obbligatoria"
    }

    AssistChip(
        onClick = { },
        label = {
            Text(
                text = "$tipoPausaText (${pausa.durata.toMinutes()}min)",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                getTipoPausaIcon(pausa.tipo),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (pausa.èRetribuita)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (pausa.èRetribuita)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun NotaItem(nota: Nota) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (nota.tipo) {
                    TipoNota.GENERALE -> Icons.Default.Info
                    TipoNota.IMPORTANTE -> Icons.Default.PriorityHigh
                    TipoNota.SICUREZZA -> Icons.Default.Warning
                    TipoNota.PROCEDURA -> Icons.Default.Bookmark
                    TipoNota.CLIENTE -> Icons.Default.Person
                    TipoNota.EQUIPMENT -> Icons.Default.Build
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (nota.tipo) {
                    TipoNota.GENERALE -> MaterialTheme.colorScheme.primary
                    TipoNota.IMPORTANTE -> MaterialTheme.colorScheme.error
                    TipoNota.SICUREZZA -> MaterialTheme.colorScheme.tertiary
                    TipoNota.PROCEDURA -> MaterialTheme.colorScheme.secondary
                    TipoNota.CLIENTE -> MaterialTheme.colorScheme.onPrimary
                    TipoNota.EQUIPMENT -> MaterialTheme.colorScheme.outline
                }
            )

            Text(
                text = nota.testo,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

