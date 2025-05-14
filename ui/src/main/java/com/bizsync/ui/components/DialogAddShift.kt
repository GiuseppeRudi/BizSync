package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.model.Turno
import com.bizsync.ui.viewmodels.DialogAddShiftViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun DialogAddShif(showDialog: Boolean, giornoSelezionato: LocalDate?, onDismiss: () -> Unit) {

    val dialogviewmodel : DialogAddShiftViewModel = viewModel()

    val text by dialogviewmodel.text.collectAsState()






    if (showDialog) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White)
        ) {

            // Input per aggiungere elementi
            OutlinedTextField(
                value = text,
                onValueChange = { dialogviewmodel.onTextChanged(it) },
                label = { Text("Aggiungi elemento") },
                modifier = Modifier.fillMaxWidth()
            )





            Spacer(modifier = Modifier.height(8.dp))

            // Bottone per aggiungere l'elemento alla lista
            Button(
                onClick = {
                    if (text.isNotEmpty() && giornoSelezionato!=null) {
                        val timestamp = localDateToTimestamp(giornoSelezionato)
                        dialogviewmodel.aggiungiturno(Turno("",dialogviewmodel.text.value,timestamp ))
                        dialogviewmodel.onTextChanged("") // Resetta il campo di input
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aggiungi")
            }


            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chiudi")
            }
        }

    }
}


fun localDateToTimestamp(localDate: LocalDate): Timestamp {

    val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()) // mezzanotte nel fuso orario locale
    val date = Date.from(startOfDay.toInstant())
    return Timestamp(date)
}

@Preview
@Composable
fun DialogAddShifPreview() {
    var giornoSelezionato = LocalDate.now()
    var showDialog by remember { mutableStateOf(true) }
    DialogAddShif(showDialog = showDialog,giornoSelezionato, onDismiss = { showDialog = false })
}