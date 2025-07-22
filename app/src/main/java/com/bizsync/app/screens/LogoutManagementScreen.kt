package com.bizsync.app.screens


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.R
import kotlinx.coroutines.delay

@Composable
fun LogoutManagementScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var showContent by remember { mutableStateOf(false) }

    // Animazioni di background
    val infiniteTransition = rememberInfiniteTransition(label = "logout_transition")

    val backgroundRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gradient colors coerenti con BizSync
    val gradientColors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460),
        Color(0xFF533A7B)
    )

    // Sequenza di messaggi e azioni
    LaunchedEffect(Unit) {
        showContent = true
        delay(2000) // Mostra messaggio di arrivederci
        currentStep = 1
        delay(2500) // Inizia pulizia cache
        currentStep = 2
        delay(2000) // Disconnessione sicura
        currentStep = 3
        delay(1500) // Completamento
        currentStep = 4
        delay(1000) // Pausa finale
        onLogout() // Esegue il logout
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Elementi decorativi di background
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size((180 + index * 60).dp)
                    .alpha(0.06f)
                    .rotate(backgroundRotation + (index * 45f))
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        CircleShape
                    )
            )
        }

        // Cerchio pulsante
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale)
                .alpha(0.15f)
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

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(800)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Logo aziendale
                    Image(
                        painter = painterResource(id = R.drawable.logobizsync),
                        contentDescription = "Logo BizSync",
                        modifier = Modifier.size(64.dp)
                    )

                    // Contenuto dinamico basato sul currentStep
                    LogoutStepContent(currentStep = currentStep)

                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (currentStep + 1) / 5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF2196F3),
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutStepContent(currentStep: Int) {
    val stepData = when (currentStep) {
        0 -> LogoutStep(
            icon = Icons.Default.Accessibility,
            title = "Arrivederci!",
            message = "Grazie per aver utilizzato BizSync.\nLa tua produttività aziendale è la nostra priorità.",
            color = Color(0xFF4CAF50)
        )
        1 -> LogoutStep(
            icon = Icons.Default.CleaningServices,
            title = "Pulizia in corso...",
            message = "Rimozione dati temporanei\ne pulizia della cache locale.",
            color = Color(0xFF2196F3)
        )
        2 -> LogoutStep(
            icon = Icons.Default.Security,
            title = "Disconnessione sicura",
            message = "Chiusura delle sessioni attive\ne protezione dei tuoi dati.",
            color = Color(0xFF9C27B0)
        )
        3 -> LogoutStep(
            icon = Icons.Default.CheckCircle,
            title = "Completato!",
            message = "Logout eseguito con successo.\nA presto su BizSync!",
            color = Color(0xFF4CAF50)
        )
        else -> LogoutStep(
            icon = Icons.Default.CheckCircle,
            title = "Fatto!",
            message = "Redirezione in corso...",
            color = Color(0xFF4CAF50)
        )
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith
                    slideOutVertically { -it } + fadeOut()
        },
        label = "step_content"
    ) { step ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icona animata
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        stepData.color.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stepData.icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = stepData.color
                )
            }

            // Titolo
            Text(
                text = stepData.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )

            // Messaggio
            Text(
                text = stepData.message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // Dots animati per i passi in corso (non per il completato)
            if (step in 1..2) {
                LogoutLoadingDots(color = stepData.color)
            }
        }
    }
}

@Composable
fun LogoutLoadingDots(color: Color = Color(0xFF2196F3)) {
    val infiniteTransition = rememberInfiniteTransition(label = "logout_dots")

    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    .size(8.dp)
                    .alpha(alpha)
                    .background(color, CircleShape)
            )
        }
    }
}

// Data class per i passi del logout
data class LogoutStep(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val color: Color
)

@Preview
@Composable
fun PreviewLogoutManagementScreen() {
    LogoutManagementScreen(
        onBackClick = {},
        onLogout = {}
    )
}