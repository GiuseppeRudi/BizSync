package com.bizsync.app.screens

import android.util.Log
import com.bizsync.app.navigation.LocalUserViewModel


import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.bizsync.domain.constants.enumClass.TipoPausa
import com.bizsync.domain.constants.enumClass.TipoNota
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.ui.components.AIResultDialog
import com.bizsync.ui.components.DipartimentoHeader
import com.bizsync.ui.components.EmptyTurniCard
import com.bizsync.ui.components.SectionHeader
import com.bizsync.ui.components.TimelineOrariaDettagliata
import com.bizsync.ui.components.getTipoPausaIcon
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate



@Composable
fun GestioneTurniDipartimentoScreen(
    dipartimento: AreaLavoro,
    giornoSelezionato: LocalDate,
    weeklyShift: WeeklyShift?,
    onCreateShift: () -> Unit,
    onBack: () -> Unit,
    weeklyIsIdentical : Boolean,
    managerVM : PianificaManagerViewModel ,
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()



    val managerState by managerVM.uiState.collectAsState()
    val turniGioDip = managerState.turniGiornalieriDip

    val showDialogCreateShift = managerState.showDialogCreateShift



    val hasChangeShift = managerState.hasChangeShift

    LaunchedEffect(Unit) {
        if (weeklyShift != null && hasChangeShift) {
            Log.d("AVVIO", "GESTIONE TURNI DIARTERNALI")
            managerVM.caricaTurniSettimanaEDipartimento(weeklyShift.weekStart,  dipartimento.id)
        }
        else {
            managerVM.setTurniGiornalieriDipartimento(dipartimento.id)
        }
    }

    LaunchedEffect(hasChangeShift) {
        if (weeklyShift != null && hasChangeShift) {
            Log.d("AVVIO", "GESTIONE TURNI DIARTERNALI")
            managerVM.caricaTurniSettimanaEDipartimento(weeklyShift.weekStart,  dipartimento.id)
        }
        else {
            managerVM.setTurniGiornalieriDipartimento(dipartimento.id)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() ,
            verticalArrangement = Arrangement.spacedBy(8.dp)

        ) {
            item {
                DipartimentoHeader(
                    dipartimento = dipartimento,
                    giornoSelezionato = giornoSelezionato,
                    onBack = onBack
                )
            }

            item {
                TimelineOrariaDettagliata(
                    dipartimento = dipartimento,
                    giornoSelezionato = giornoSelezionato,
                    turniAssegnati = turniGioDip
                )
            }

            item {
                SectionHeader("Turni Assegnati", "${turniGioDip.size} turni")
            }

            if (turniGioDip.isNotEmpty()) {
                items(turniGioDip) { turno ->
                    TurnoAssegnatoCard(
                        turno = turno,
                        isIdentical = weeklyIsIdentical,
                        onEdit = { managerVM.editTurno() },
                        onDelete = { managerVM.deleteTurno() },
                        dipendenti = managerState.dipendenti
                    )
                }
            } else {
                item {
                    EmptyTurniCard()
                }
            }

        }


        if(weeklyIsIdentical)
        {
            // ✅ FAB posizionato correttamente sopra la LazyColumn
            FloatingActionButton(
                onClick =  onCreateShift ,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Turno")
            }
        }

        FloatingActionButton(
            onClick = {
                managerVM.generateTurniWithAI(
                    dipartimento = dipartimento,
                    giornoSelezionato = giornoSelezionato
                )
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome, // Icona AI
                contentDescription = "Genera Turni con AI"
            )
        }

// Dialog per mostrare risultati AI
        if (managerState.showAIResultDialog) {
            AIResultDialog(
                turniGenerati = managerState.turniGeneratiAI,
                message = managerState.aiGenerationMessage,
                onConfirm = { managerVM.confermaAITurni() },
                onDismiss = { managerVM.annullaAITurni() }
            )
        }

// Loading indicator per generazione AI
        if (managerState.isGeneratingTurni) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Generazione turni con AI in corso...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

    }


}
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
            // Header compatto sempre visibile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Informazioni principali
                Column(modifier = Modifier.weight(1f)) {
                    // Titolo del turno
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
                            text = "${turno.orarioInizio} - ${turno.orarioFine}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dipendenti (primi 2-3 nomi)
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

                // Pulsanti azione
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsante espandi
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
                icon = Icons.Default.Notes
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

        // Informazioni aggiuntive
        DetailSection(
            title = "Informazioni",
            icon = Icons.Default.Info
        ) {
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

                InfoRow(
                    label = "ID Turno",
                    value = turno.id.take(8) + "...",
                    icon = Icons.Default.Tag
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
    color: androidx.compose.ui.graphics.Color
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
        onClick = { /* Opzionale: mostra dettagli dipendente */ },
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
                    TipoNota.CLIENTE -> TODO()
                    TipoNota.EQUIPMENT -> TODO()
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (nota.tipo) {
                    TipoNota.GENERALE -> MaterialTheme.colorScheme.primary
                    TipoNota.IMPORTANTE -> MaterialTheme.colorScheme.error
                    TipoNota.SICUREZZA -> MaterialTheme.colorScheme.tertiary
                    TipoNota.PROCEDURA -> MaterialTheme.colorScheme.secondary
                    TipoNota.CLIENTE -> TODO()
                    TipoNota.EQUIPMENT -> TODO()
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

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}








