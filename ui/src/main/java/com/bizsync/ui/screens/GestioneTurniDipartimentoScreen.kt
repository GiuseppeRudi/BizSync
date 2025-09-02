package com.bizsync.ui.screens

import android.util.Log
import com.bizsync.ui.navigation.LocalUserViewModel
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.ui.components.AIResultDialog
import com.bizsync.ui.components.DipartimentoHeader
import com.bizsync.ui.components.EmptyTurniCard
import com.bizsync.ui.components.TimelineOrariaDettagliata
import com.bizsync.ui.components.TurnoAssegnatoCard
import com.bizsync.ui.components.getTipoPausaIcon
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun GestioneTurniDipartimentoScreen(
    dipartimento: AreaLavoro,
    giornoSelezionato: LocalDate,
    weeklyShift: WeeklyShift?,
    onCreateShift: () -> Unit,
    onBack: () -> Unit,
    weeklyIsIdentical : Boolean,
    onHasUnsavedChanges : (Boolean) -> Unit,
    managerVM : PianificaManagerViewModel ,
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()

    val idAzienda = userState.azienda.idAzienda

    val managerState by managerVM.uiState.collectAsState()
    val turniGioDip = managerState.turniGiornalieriDip


    val scaffoldVm = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
      scaffoldVm.onFullScreenChanged(false)
    }


    val hasChangeShift = managerState.hasChangeShift

    LaunchedEffect(Unit) {
        if (weeklyShift != null && hasChangeShift) {
            managerVM.caricaTurniSettimanaEDipartimento(weeklyShift.weekStart,  giornoSelezionato.dayOfWeek,weeklyShift.dipartimentiAttivi)
        }
        managerVM.setTurniGiornalieriDipartimento(dipartimento.nomeArea)

    }

    LaunchedEffect(hasChangeShift) {
        if (weeklyShift != null && hasChangeShift) {
            onHasUnsavedChanges(true)
            managerVM.caricaTurniSettimanaEDipartimento(weeklyShift.weekStart,  giornoSelezionato.dayOfWeek, weeklyShift.dipartimentiAttivi)
        }
        managerVM.setTurniGiornalieriDipartimento(dipartimento.nomeArea)

    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        DeleteTurnoConfirmDialog(
            showDialog = managerState.showDeleteConfirmDialog,
            turnoToDelete = managerState.turnoToDelete,
            onConfirm = { managerVM.confirmDeleteTurno() },
            onDismiss = { managerVM.cancelDeleteTurno() }
        )

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turni Assegnati (${turniGioDip.size} turni)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f) // Occupa tutto lo spazio disponibile
                    )

                    if (weeklyIsIdentical && weeklyShift?.status != WeeklyShiftStatus.PUBLISHED) {
                        FloatingActionButton(
                            onClick = {
                                managerVM.generateTurniWithAI(
                                    dipartimento = dipartimento,
                                    giornoSelezionato = giornoSelezionato,
                                    idAzienda = idAzienda
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp) // opzionale per rendere più piccoli
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Genera con AI")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = onCreateShift,
                            modifier = Modifier.size(40.dp) // opzionale per rendere più piccoli
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aggiungi Turno")
                        }
                    }
                }
            }


            if (turniGioDip.isNotEmpty()) {
                items(turniGioDip) { turno ->
                    TurnoAssegnatoCard(
                        turno = turno,
                        isIdentical = weeklyIsIdentical && weeklyShift?.status != WeeklyShiftStatus.PUBLISHED,
                        onEdit = { managerVM.editTurno(turno.id)
                                 onCreateShift()
                                 },
                        onDelete = { managerVM.deleteTurnoWithConfirmation(turno.id) {}
                        },
                        dipendenti = managerState.dipendenti.filter { it.uid in turno.idDipendenti }
                    )
                }
            } else {
                item {
                    EmptyTurniCard()
                }
            }

        }




        if (managerState.showAIResultDialog) {
            AIResultDialog(
                dipendenti = managerState.dipendenti,
                turniGenerati = managerState.turniGeneratiAI,
                message = managerState.aiGenerationMessage,
                onConfirm = { managerVM.confermaAITurni() },
                onDismiss = { managerVM.annullaAITurni() }
            )
        }

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
fun DeleteTurnoConfirmDialog(
    showDialog: Boolean,
    turnoToDelete: Turno?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog && turnoToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Elimina Turno",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Sei sicuro di voler eliminare il turno:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = turnoToDelete.titolo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "${turnoToDelete.orarioInizio} - ${turnoToDelete.orarioFine}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            val assignedCount = turnoToDelete.idDipendenti.size
                            val assignedLabel = if (assignedCount == 1) "dipendente assegnato" else "dipendenti assegnati"

                            Text(
                                text = "$assignedCount $assignedLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Questa azione non può essere annullata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontStyle = FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elimina")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}








