package com.bizsync.app.screens

import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.GestioneTurniDipartimentoViewModel


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.components.AnalisiCoperturaCard
import com.bizsync.ui.components.AzioniCompletamentoCard
import com.bizsync.ui.components.DipartimentoHeader
import com.bizsync.ui.components.EmptyTurniCard
import com.bizsync.ui.components.ErrorScreen
import com.bizsync.ui.components.SectionHeader
import com.bizsync.ui.components.SuggerimentiCard
import com.bizsync.ui.components.TimelineOrariaDettagliata
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate



@Composable
fun GestioneTurniDipartimentoScreen(
    dipartimento: AreaLavoro,
    giornoSelezionato: LocalDate,
    onBack: () -> Unit,
    managerVM : PianificaManagerViewModel ,
    viewModel: GestioneTurniDipartimentoViewModel = hiltViewModel()
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()



    val managerState by managerVM.uiState.collectAsState()
    val turniGioDip = managerState.turniGiornalieriDip


//    LaunchedEffect(Unit) {
//        managerVM.setTurniGiornalieriDip(dipartimento.id, giornoSelezionato.dayOfWeek)
//    }

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
                        onEdit = { viewModel.editTurno(turno) },
                        onDelete = { viewModel.deleteTurno(turno) }
                    )
                }
            } else {
                item {
                    EmptyTurniCard()
                }
            }

            item {
                AnalisiCoperturaCard(
                    dipartimento = dipartimento,
                    giornoSelezionato = giornoSelezionato,
                    turniAssegnati = turniGioDip
                )
            }

//            // Suggerimenti se disponibili
//            if (uiState.suggerimenti.isNotEmpty()) {
//                item {
//                    SuggerimentiCard(
//                        suggerimenti = uiState.suggerimenti,
//                        onApplySuggerimento = { viewModel.applySuggerimento(it) }
//                    )
//                }
//            }

            // Azioni completamento come ultimo item
            item {
//                AzioniCompletamentoCard(
//                    stato = uiState.stato,
//                    onSegnaCompletato = { viewModel.segnaDipartimentoCompletato() },
//                    onTornaIndietro = onBack
//                )
            }
        }

        // ✅ FAB posizionato correttamente sopra la LazyColumn
        FloatingActionButton(
            onClick = { viewModel.showAddTurnoDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Aggiungi Turno")
        }
    }

//    // ✅ Dialog fuori dal layout principale
//    if (uiState.showTurnoDialog) {
//        AddEditTurnoDialog(
//            turno = uiState.turnoInModifica,
//            dipartimento = dipartimento,
//            giornoSelezionato = giornoSelezionato,
//            onSave = { viewModel.saveTurno(it) },
//            onDismiss = { viewModel.hideTurnoDialog() }
//        )
//    }
}

@Composable
fun TurnoAssegnatoCard(
    turno: Turno,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = turno.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Orario: ${turno.orarioInizio} - ${turno.orarioFine}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (turno.dipendente.isNotEmpty()) {
                    Text(
                        text = "Assegnato a: ${turno.dipendente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row {
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
}






