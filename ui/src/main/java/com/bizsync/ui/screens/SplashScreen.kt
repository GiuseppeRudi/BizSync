package com.bizsync.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.bizsync.ui.R

@Composable
fun SplashScreenWithProgress(
    isDataReady: Boolean,
    elapsedTime: Long,
    minimumDuration: Long
) {
    val animationProgress = (elapsedTime.toFloat() / minimumDuration).coerceIn(0f, 1f)

    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val backgroundRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
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

    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (showLogo) 0f else -180f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "logoRotation"
    )

    // Gradient colors
    val gradientColors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460),
        Color(0xFF533A7B)
    )

    LaunchedEffect(Unit) {
        delay(300)
        showLogo = true
        delay(800)
        showText = true
        delay(400)
        showSubtitle = true
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
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((200 + index * 100).dp)
                    .alpha(0.1f)
                    .rotate(backgroundRotation + (index * 30f))
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(pulseScale)
                .alpha(0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64B5F6).copy(alpha = 0.4f),
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
            AnimatedVisibility(
                visible = showLogo,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logobizsync),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                            rotationZ = logoRotation
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showText,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "BizSync",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showSubtitle,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(500, delayMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500, delayMillis = 200))
            ) {
                Text(
                    text = "Gestione Turni Intelligente",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingIndicatorWithStatus(
                        isDataReady = isDataReady,
                        progress = animationProgress
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Testo di stato
                    Text(
                        text = when {
                            !isDataReady -> "Caricamento dati..."
                            animationProgress < 1f -> "Preparazione completata..."
                            else -> "Avvio applicazione..."
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingIndicatorWithStatus(
    isDataReady: Boolean,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingIndicator()

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(3.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(1.5.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF64B5F6),
                                Color(0xFF42A5F5)
                            )
                        ),
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isDataReady) Color.Green else Color.Yellow,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isDataReady) "Dati pronti" else "Caricamento...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

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
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        CircleShape
                    )
            )
        }
    }
}



