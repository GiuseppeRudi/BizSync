package com.bizsync.app.screens


import android.text.Layout.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bizsync.app.navigation.ModelProvider
import androidx.compose.runtime.getValue

import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val coroutineScope = rememberCoroutineScope()

    var responseText by remember { mutableStateOf<String?>(null) }
    var isLoading    by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            responseText != null -> {
                Text(text = responseText!!)
            }
            else -> {
                Button(onClick = {
                    isLoading = true
                    responseText = null

                    // Qui lancio la coroutine, non un LaunchedEffect
                    coroutineScope.launch {
                        try {
                            val result = ModelProvider.model
                                .generateContent("Ciao, come stai?")
                            responseText = result.text
                        } catch (e: Exception) {
                            responseText = "Errore: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Invia prompt di prova")
                }
            }
        }
    }
}
