package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.model.domain.Turno
import com.bizsync.ui.viewmodels.PianificaViewModel
import com.bizsync.ui.viewmodels.TurnoViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun TurnoDialog(
    showDialog: Boolean,
    giornoSelezionato: LocalDate?,
    onDismiss: () -> Unit,
    pianificaVM: PianificaViewModel
) {
    val turnoVM: TurnoViewModel = hiltViewModel()

    val text by turnoVM.text.collectAsState()
    var selectedArea by remember { mutableStateOf("Logistica") }
    var startHour by remember { mutableStateOf("") }
    var endHour by remember { mutableStateOf("") }
    var numPause by remember { mutableStateOf(0) }
    var membri by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val aree = listOf("Logistica", "Training", "Sicurezza", "IT", "Customer Service")

    if (showDialog) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { turnoVM.onTextChanged(it) },
                label = { Text("Titolo turno") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Area di lavoro
            DropdownMenuArea(selectedArea, aree) { selectedArea = it }

            Spacer(Modifier.height(8.dp))

            // Fascia oraria
            Row {
                OutlinedTextField(
                    value = startHour,
                    onValueChange = { startHour = it },
                    label = { Text("Inizio (es. 09:00)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = endHour,
                    onValueChange = { endHour = it },
                    label = { Text("Fine (es. 17:00)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Numero di pause
            OutlinedTextField(
                value = numPause.toString(),
                onValueChange = { numPause = it.toIntOrNull() ?: 0 },
                label = { Text("Numero di pause") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Membri assegnati (potresti poi sostituirlo con una lista selezionabile)
            OutlinedTextField(
                value = membri,
                onValueChange = { membri = it },
                label = { Text("Membri assegnati (es. Mario, Luca)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (opzionali)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (text.isNotEmpty() && giornoSelezionato != null) {
                        val timestamp = localDateToTimestamp(giornoSelezionato)
                        turnoVM.aggiungiturno(pianificaVM,
                            Turno(
                                idDocumento = "",
                                nome = text,
                                giorno = timestamp,
                                /*area = selectedArea,
                                fasciaOraria = "$startHour - $endHour",
                                pause = numPause,
                                membri = membri.split(",").map { it.trim() },
                                note = note*/
                            )
                        )
                        turnoVM.onTextChanged("")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aggiungi Turno")
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Chiudi")
            }
        }
    }
}

@Composable
fun DropdownMenuArea(selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text("Area di lavoro") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}


fun localDateToTimestamp(localDate: LocalDate): Timestamp {

    val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()) // mezzanotte nel fuso orario locale
    val date = Date.from(startOfDay.toInstant())
    return Timestamp(date)
}

