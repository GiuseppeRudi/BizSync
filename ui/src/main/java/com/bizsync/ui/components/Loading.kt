package com.bizsync.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.ui.R

@Composable
fun BizSyncLoader(
    message: String = "Caricamento...",
    showLogo: Boolean = true,
    isFullScreen: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Animazioni infinite per effetti di background
    val infiniteTransition = rememberInfiniteTransition(label = "loader_transition")

    val backgroundRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gradient colors dal tuo splash screen
    val gradientColors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460),
        Color(0xFF533A7B)
    )

    val containerModifier = if (isFullScreen) {
        modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    radius = 1000f
                )
            )
    } else {
        modifier
            .background(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    radius = 400f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        // Elementi decorativi di background (solo se fullscreen)
        if (isFullScreen) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((150 + index * 80).dp)
                        .alpha(0.08f)
                        .rotate(backgroundRotation + (index * 30f))
                        .background(
                            Color.White.copy(alpha = 0.05f),
                            CircleShape
                        )
                )
            }
        }

        // Cerchio pulsante dietro al contenuto
        Box(
            modifier = Modifier
                .size(if (isFullScreen) 200.dp else 120.dp)
                .scale(pulseScale)
                .alpha(0.2f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64B5F6).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo (opzionale)
            if (showLogo) {
                Image(
                    painter = painterResource(id = R.drawable.logobizsync),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(if (isFullScreen) 80.dp else 48.dp)
                        .alpha(0.9f)
                )

                Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))
            }

            // Messaggio di caricamento
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = if (isFullScreen) 18.sp else 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isFullScreen) 24.dp else 16.dp))

            // Indicatore di caricamento con i punti animati
            BizSyncLoadingDots()
        }
    }
}

@Composable
fun BizSyncLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_loading")

    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha = when (index) {
                0 -> dotAlpha1
                1 -> dotAlpha2
                else -> dotAlpha3
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha)
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        CircleShape
                    )
            )
        }
    }
}

// Variante compatta per dialog o componenti piccoli
@Composable
fun BizSyncCompactLoader(
    message: String = "Caricamento...",
    modifier: Modifier = Modifier
) {
    BizSyncLoader(
        message = message,
        showLogo = false,
        isFullScreen = false,
        modifier = modifier
            .width(250.dp)
            .height(150.dp)
    )
}

// Variante per overlay su contenuto esistente
@Composable
fun BizSyncOverlayLoader(
    message: String = "Caricamento...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .width(280.dp)
                .height(180.dp)
        ) {
            BizSyncLoader(
                message = message,
                showLogo = true,
                isFullScreen = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// PREVIEW COMPONENTS
@Preview(showBackground = true)
@Composable
fun PreviewBizSyncLoader() {
    BizSyncLoader("Caricamento dati aziendali...")
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewCompactLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        BizSyncCompactLoader("Elaborazione...")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOverlayLoader() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Contenuto di esempio
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Contenuto della schermata", fontSize = 20.sp)
            Text("Altri elementi...", fontSize = 16.sp)
        }

        // Overlay loader
        BizSyncOverlayLoader("Sincronizzazione in corso...")
    }
}

// ESEMPI DI UTILIZZO
/*
// Per schermo intero di caricamento
BizSyncLoader("Caricamento dashboard...")

// Per componenti compatti
BizSyncCompactLoader("Salvataggio...")

// Per overlay su contenuto esistente
if (isLoading) {
    BizSyncOverlayLoader("Elaborazione dati...")
}

// In un LazyColumn con item di loading
LazyColumn {
    items(data) { item ->
        // ... item content
    }

    if (isLoadingMore) {
        item {
            BizSyncCompactLoader("Caricamento altri elementi...")
        }
    }
}
*/