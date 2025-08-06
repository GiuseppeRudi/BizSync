package com.bizsync.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.domain.constants.enumClass.CleanupStep
import com.bizsync.ui.model.LogoutStepUi
import com.bizsync.ui.viewmodels.LogoutViewModel
import kotlinx.coroutines.delay
import com.bizsync.ui.R
@Composable
fun LogoutManagementScreen(
    onLogout: () -> Unit,
    cleanupViewModel: LogoutViewModel = hiltViewModel()
) {
    val scaffoldVM = LocalScaffoldViewModel.current
    val cleanupState by cleanupViewModel.uiState.collectAsStateWithLifecycle()

    var currentStep by remember { mutableIntStateOf(0) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "logout_transition")

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
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gradient colors
    val gradientColors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460),
        Color(0xFF533A7B)
    )


    LaunchedEffect(Unit) {
        showContent = true
        delay(700)
        currentStep = 1

        cleanupViewModel.startCleanup()

        delay(800)
        currentStep = 2
        delay(800)
        onLogout()
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
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size((160 + index * 40).dp)
                    .alpha(0.08f)
                    .rotate(backgroundRotation + (index * 60f))
                    .background(
                        Color.White.copy(alpha = 0.06f),
                        CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .alpha(0.12f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64B5F6).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.96f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Logo aziendale
                    Image(
                        painter = painterResource(id = R.drawable.logobizsync),
                        contentDescription = "Logo BizSync",
                        modifier = Modifier.size(56.dp)
                    )

                    LogoutStepContent(
                        currentStep = currentStep,
                        cleanupStep = cleanupState.currentStep
                    )

                    LinearProgressIndicator(
                    progress = {
                        when (currentStep) {
                            0 -> 0.2f
                            1 -> 0.7f
                            else -> 1f }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFF2196F3),
                    trackColor = Color.Gray.copy(alpha = 0.2f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutStepContent(
    currentStep: Int,
    cleanupStep: CleanupStep
) {
    val stepData = when (currentStep) {
        0 -> LogoutStepUi(
            icon = Icons.Default.CheckCircle,
            title = "Arrivederci!",
            message = "Grazie per aver utilizzato BizSync",
            color = Color(0xFF4CAF50)
        )
        1 -> LogoutStepUi(
            icon = Icons.Default.CleaningServices,
            title = getCleanupTitle(cleanupStep),
            message = cleanupStep.message,
            color = Color(0xFF2196F3)
        )
        else -> LogoutStepUi(
            icon = Icons.Default.CheckCircle,
            title = "Completato!",
            message = "A presto su BizSync!",
            color = Color(0xFF4CAF50)
        )
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            slideInVertically { it / 2 } + fadeIn(
                animationSpec = tween(300)
            ) togetherWith
                    slideOutVertically { -it / 2 } + fadeOut(
                animationSpec = tween(300)
            )
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
                    .size(64.dp)
                    .background(
                        stepData.color.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stepData.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = stepData.color
                )
            }

            // Titolo
            Text(
                text = stepData.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )

            // Messaggio
            Text(
                text = stepData.message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (step == 1) {
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
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    .size(6.dp)
                    .alpha(alpha)
                    .background(color, CircleShape)
            )
        }
    }
}

private fun getCleanupTitle(cleanupStep: CleanupStep): String {
    return when (cleanupStep) {
        CleanupStep.STARTING -> "Inizializzazione..."
        CleanupStep.CLEARING_CACHE -> "Pulizia cache..."
        CleanupStep.CLEARING_PREFERENCES -> "Pulizia preferenze..."
        CleanupStep.COMPLETED -> "Pulizia completata!"
        CleanupStep.ERROR -> "Errore"
    }
}

