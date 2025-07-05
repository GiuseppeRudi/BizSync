package com.bizsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.TurnoFrequente


@Composable
fun TurnoFrequenteDialog(
    turno: TurnoFrequente?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf(turno?.nome ?: "") }
    var oraInizio by remember { mutableStateOf(turno?.oraInizio ?: "") }
    var oraFine by remember { mutableStateOf(turno?.oraFine ?: "") }

    var nomeError by remember { mutableStateOf(false) }
    var oraInizioError by remember { mutableStateOf(false) }
    var oraFineError by remember { mutableStateOf(false) }

    // Time picker states
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (turno == null) "Nuovo Turno Frequente" else "Modifica Turno Frequente"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        nomeError = it.isBlank()
                    },
                    label = { Text("Nome Turno") },
                    isError = nomeError,
                    supportingText = if (nomeError) {
                        { Text("Il nome del turno è obbligatorio") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = oraInizio,
                        onValueChange = { },
                        label = { Text("Ora Inizio") },
                        readOnly = true,
                        isError = oraInizioError,
                        supportingText = if (oraInizioError) {
                            { Text("Obbligatorio") }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartTimePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showStartTimePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Seleziona ora"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = oraFine,
                        onValueChange = { },
                        label = { Text("Ora Fine") },
                        readOnly = true,
                        isError = oraFineError,
                        supportingText = if (oraFineError) {
                            { Text("Obbligatorio") }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndTimePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showEndTimePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Seleziona ora"
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hasErrors = listOf(
                        nome.isBlank().also { nomeError = it },
                        oraInizio.isBlank().also { oraInizioError = it },
                        oraFine.isBlank().also { oraFineError = it }
                    ).any { it }

                    if (!hasErrors) {
                        onConfirm(nome, oraInizio, oraFine)
                    }
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )

    // Time Pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = parseTime(oraInizio),
            onTimeSelected = { hour, minute ->
                oraInizio = String.format("%02d:%02d", hour, minute)
                oraInizioError = false
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = parseTime(oraFine),
            onTimeSelected = { hour, minute ->
                oraFine = String.format("%02d:%02d", hour, minute)
                oraFineError = false
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: Pair<Int, Int>,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.first,
        initialMinute = initialTime.second,
        is24Hour = true
    )

    var showingPicker by remember { mutableStateOf(true) }

    if (showingPicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

private fun parseTime(timeString: String): Pair<Int, Int> {
    return if (timeString.contains(":")) {
        val parts = timeString.split(":")
        try {
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(9, 0) // Default
        }
    } else {
        Pair(9, 0) // Default
    }
}
