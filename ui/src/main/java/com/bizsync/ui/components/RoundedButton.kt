package com.bizsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

import androidx.compose.ui.unit.dp

@Composable
fun RoundedButton(enabled : Boolean = false, onShow: () -> Unit, modifier : Modifier) {
    var buttonText by remember { mutableStateOf("+") }

    // Bottone con angoli arrotondati
    Button(
        enabled = enabled ,
        onClick = { onShow() },  // Cosa fare quando il bottone è cliccato
        shape = MaterialTheme.shapes.extraLarge,         // Angoli arrotondati con il tema Material
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)), // Colore del bottone
        modifier = modifier         // Distanza dal bordo
    ) {
        Text(text = buttonText, color = Color.White) // Testo del bottone
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRoundedButton() {

}
