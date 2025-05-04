package com.bizsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ErrorDialog(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    if (errorMessage != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss
        ) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Errore", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text("Chiudi")
                    }
                }
            }
        }
    }
}
