package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitoloTurnoField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            label = { Text("Titolo turno") },
            placeholder = { Text("Es. Turno Mattutino - Produzione") },
            leadingIcon = {
                Icon(
                    Icons.Default.Title,
                    contentDescription = "Titolo",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            supportingText = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isError && errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = when {
                                    value.isEmpty() -> "Inserisci un titolo descrittivo"
                                    value.length < 5 -> "Titolo troppo breve"
                                    value.length > 50 -> "Titolo troppo lungo"
                                    else -> "Perfetto! ✓"
                                },
                                color = when {
                                    value.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                                    value.length < 5 || value.length > 50 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "${value.length}/50",
                            color = if (value.length > 50) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            isError = isError || value.length > 50,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

    }
}