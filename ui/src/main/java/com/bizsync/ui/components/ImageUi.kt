package com.bizsync.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun EmployeeAvatar(photoUrl: String?, nome: String, cognome: String, size: Dp = 56.dp) {
    var showFallback by remember { mutableStateOf(false) }

    if (!photoUrl.isNullOrBlank() && !showFallback) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Foto profilo",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = {
                Log.e("EmployeeAvatar", "Errore caricamento immagine: $photoUrl")
                showFallback = true
            },
            onLoading = {
                // Mostra un placeholder durante il caricamento
            }
        )
    } else {
        // Fallback con iniziali
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${nome.firstOrNull()?.uppercaseChar() ?: ""}${cognome.firstOrNull()?.uppercaseChar() ?: ""}",
                color = Color.White,
                fontSize = (size.value * 0.32).sp, // Dimensione proporzionale
                fontWeight = FontWeight.Bold
            )
        }
    }
}

