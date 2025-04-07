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
import com.bizsync.ui.viewmodels.DialogAddShiftViewModel

@Composable
fun DialogAddShif(showDialog: Boolean, onDismiss: () -> Unit) {

    val dialogviewmodel : DialogAddShiftViewModel = viewModel()


    if (showDialog) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White)
        ) {

            // Input per aggiungere elementi
            OutlinedTextField(
                value = dialogviewmodel.text.value,
                onValueChange = { dialogviewmodel.text.value = it },
                label = { Text("Aggiungi elemento") },
                modifier = Modifier.fillMaxWidth()
            )




            Spacer(modifier = Modifier.height(8.dp))

            // Bottone per aggiungere l'elemento alla lista
            Button(
                onClick = {
                    if (dialogviewmodel.text.value.isNotEmpty()) {
                        //dialogviewmodel.itemsList.add(dialogviewmodel.text.value)
                        dialogviewmodel.text.value = "" // Resetta il campo di input
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


@Preview
@Composable
fun DialogAddShifPreview() {
    var showDialog by remember { mutableStateOf(true) }
    DialogAddShif(showDialog = showDialog, onDismiss = { showDialog = false })
}