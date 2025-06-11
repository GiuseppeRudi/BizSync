package com.bizsync.app.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.model.domain.Turno
import com.bizsync.ui.viewmodels.PianificaViewModel
import com.bizsync.ui.viewmodels.TurnoViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import androidx.compose.runtime.getValue
import com.bizsync.model.domain.AreaLavoro
import com.bizsync.ui.viewmodels.UserViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import com.bizsync.model.domain.Pausa
import com.bizsync.ui.components.AreeLavoroSelector
import com.bizsync.ui.components.PauseManagerDialog
import com.bizsync.ui.components.TimeRangePicker
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoScreen(
    giornoSelezionato: LocalDate?,
    onBack: () -> Unit,
    pianificaVM: PianificaViewModel,
    userVM: UserViewModel,
    scaffoldVM : ScaffoldViewModel
) {
    val turnoVM: TurnoViewModel = hiltViewModel()
    val azienda by userVM.azienda.collectAsState()
    val text by turnoVM.text.collectAsState()

    var membriSelezionatiIds by remember { mutableStateOf(listOf<String>()) }
    var showMembriDialog by remember { mutableStateOf(false) }

    // Assumendo che tu abbia accesso ai membri dell'azienda
    val membriTeam = turnoVM.membriDiProva // o da dove li recuperi

    // Membri selezionati completi
    val membriSelezionati = remember(membriSelezionatiIds, membriTeam) {
        membriTeam.filter { it.id in membriSelezionatiIds }
    }


    var startHour by remember { mutableStateOf("") }
    var endHour by remember { mutableStateOf("") }
    var numPause by remember { mutableStateOf(0) }
    var membri by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf<AreaLavoro?>(null) }

    val fullScreen by scaffoldVM.fullScreen.collectAsState()
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }



    if (fullScreen)
    {
        CircularProgressIndicator()
    }

    else
    {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nuovo turno") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { turnoVM.onTextChanged(it) },
                    label = { Text("Titolo turno") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                AreeLavoroSelector(
                    selectedArea = selectedArea,
                    areas = azienda.areeLavoro,
                    onAreaSelected = { area -> selectedArea = area },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TimeRangePicker(
                    startTime = startHour,
                    endTime = endHour,
                    onStartTimeSelected = { startHour = it },
                    onEndTimeSelected = { endHour = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(15.dp))

// Sostituisci la variabile numPause con:
                var pause by remember { mutableStateOf(listOf<Pausa>()) }
                var showPauseDialog by remember { mutableStateOf(false) }

// Sostituisci il campo OutlinedTextField delle pause con:
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showPauseDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pause configurate",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${pause.size} pause • ${pause.sumOf { it.durataminuti }} min totali",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Configura pause"
                        )
                    }
                }

                PauseManagerDialog(
                    showDialog = showPauseDialog,
                    pause = pause,
                    onDismiss = { showPauseDialog = false },
                    onPauseUpdated = { nuovePause -> pause = nuovePause }
                )


                Spacer(Modifier.height(8.dp))


                // Sostituisci il vecchio OutlinedTextField con:
                MembriSelezionatiSummary(
                    membriSelezionati = membriSelezionati,
                    onClick = { showMembriDialog = true }
                )

                // Dialog per selezione membri
                MembriSelectionDialog(
                    showDialog = showMembriDialog,
                    tuttiIMembri = membriTeam,
                    membriSelezionati = membriSelezionatiIds,
                    onDismiss = { showMembriDialog = false },
                    onMembriUpdated = { nuoviIds -> membriSelezionatiIds = nuoviIds }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (opzionali)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = {
                        if (text.isNotEmpty() && giornoSelezionato != null) {
                            val timestamp = localDateToTimestamp(giornoSelezionato)
                            turnoVM.aggiungiturno(
                                pianificaVM,
                                Turno(
                                    idDocumento = "",
                                    nome = text,
                                    giorno = timestamp,
                                    // Altri campi qui quando li abiliti
                                )
                            )
                            turnoVM.onTextChanged("")
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aggiungi Turno")
                }
            }
        }
    }

}



fun localDateToTimestamp(localDate: LocalDate): Timestamp {

    val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()) // mezzanotte nel fuso orario locale
    val date = Date.from(startOfDay.toInstant())
    return Timestamp(date)
}

