package com.bizsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bizsync.domain.model.AreaLavoro

@Composable
fun AreaLavoroDialog(
    area: AreaLavoro?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nomeArea by remember { mutableStateOf(area?.nomeArea ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (area == null) "Nuova Area di Lavoro" else "Modifica Area di Lavoro"
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nomeArea,
                    onValueChange = {
                        nomeArea = it
                        isError = it.isBlank()
                    },
                    label = { Text("Nome Area") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Il nome dell'area è obbligatorio") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nomeArea.isNotBlank()) {
                        onConfirm(nomeArea)
                    } else {
                        isError = true
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
}
