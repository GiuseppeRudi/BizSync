package com.bizsync.app.screens

import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.GestioneTurniDipartimentoViewModel


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.components.DipartimentoHeader
import com.bizsync.ui.components.EmptyTurniCard
import com.bizsync.ui.components.SectionHeader
import com.bizsync.ui.components.TimelineOrariaDettagliata
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.LocalDate



@Composable
fun GestioneTurniDipartimentoScreen(
    dipartimento: AreaLavoro,
    giornoSelezionato: LocalDate,
    onCreateShift: () -> Unit,
    onBack: () -> Unit,
    weeklyIsIdentical : Boolean,
    managerVM : PianificaManagerViewModel ,
    viewModel: GestioneTurniDipartimentoViewModel = hiltViewModel()
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()



    val managerState by managerVM.uiState.collectAsState()
    val turniGioDip = managerState.turniGiornalieriDip

    val showDialogCreateShift = managerState.showDialogCreateShift


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
                        isIdentical = weeklyIsIdentical,
                        onEdit = { viewModel.editTurno(turno) },
                        onDelete = { viewModel.deleteTurno(turno) }
                    )
                }
            } else {
                item {
                    EmptyTurniCard()
                }
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

    }


}

@Composable
fun TurnoAssegnatoCard(
    turno: Turno,
    isIdentical: Boolean,
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
                    text = turno.titolo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Orario: ${turno.orarioInizio} - ${turno.orarioFine}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            if(isIdentical)
            {
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
}






