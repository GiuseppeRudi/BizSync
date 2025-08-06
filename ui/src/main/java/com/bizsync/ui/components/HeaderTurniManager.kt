package com.bizsync.ui.components


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.utils.WeeklyWindowCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@Composable
fun HeaderTurniManagerWithLoading(
    loading: Boolean,
    weeklyShiftAttuale: WeeklyShift?,
    weeklyShiftRiferimento: WeeklyShift?,
    selectionData: LocalDate?
) {
    AnimatedContent(
        targetState = loading,
        transitionSpec = {
            if (targetState) {
                // Entrata placeholder
                fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.95f)
            } else {
                // Entrata contenuto reale
                fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 3 }
            }.togetherWith(
                fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 1.05f)
            )
        },
        label = "header_loading_animation"
    ) { isLoading ->
        if (!isLoading)  {
            // Logica esistente per mostrare l'header reale
            when {
                weeklyShiftAttuale != null && weeklyShiftAttuale.id != weeklyShiftRiferimento?.id -> {
                    HeaderTurniManager(weeklyShift = weeklyShiftAttuale)
                }
                weeklyShiftAttuale?.id == weeklyShiftRiferimento?.id && weeklyShiftRiferimento?.status == WeeklyShiftStatus.PUBLISHED -> {
                    HeaderTurniManager(weeklyShift = weeklyShiftAttuale)
                }
                weeklyShiftAttuale == null && selectionData != null -> {
                    HeaderTurniManager(weeklyShift = null, selectionData)
                }
            }
        }
    }
}


@Composable
fun HeaderTurniManager(
    weeklyShift: WeeklyShift?,
    giornoSelezionato: LocalDate? = null,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { -it / 2 }
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                // Header con animazione moderna
                AnimatedContent(
                    targetState = "Settimana Lavorativa",
                    transitionSpec = {
                        (slideInVertically(
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) { it / 4 } + fadeIn(
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(300, easing = FastOutLinearInEasing)
                            ) { -it / 4 } + fadeOut(
                                animationSpec = tween(300, easing = FastOutLinearInEasing)
                            )
                        )
                    },
                    label = "header_animation"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date range con animazione
                AnimatedContent(
                    targetState = when {
                        weeklyShift != null -> {
                            val weekStart = weeklyShift.weekStart
                            val weekEnd = weekStart.plusDays(6)
                            "Dal ${weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))} al ${weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                        }
                        giornoSelezionato != null -> {
                            val bounds = WeeklyWindowCalculator.getWeekBounds(giornoSelezionato)
                            "Dal ${bounds.first.format(DateTimeFormatter.ofPattern("dd/MM"))} al ${bounds.second.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                        }
                        else -> ""
                    },
                    transitionSpec = {
                        (slideInHorizontally(
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) { it / 3 } + fadeIn(
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300, easing = FastOutLinearInEasing)
                            ) { -it / 3 } + fadeOut(
                                animationSpec = tween(300, easing = FastOutLinearInEasing)
                            )
                        )
                    },
                    label = "date_range_animation"
                ) { dateText ->
                    if (dateText.isNotEmpty()) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status section con animazioni moderne
                AnimatedContent(
                    targetState = weeklyShift,
                    transitionSpec = {
                        (slideInVertically(
                            animationSpec = tween(
                                durationMillis = 600,
                                easing = FastOutSlowInEasing
                            )
                        ) { it / 2 } + fadeIn(
                            animationSpec = tween(
                                durationMillis = 600,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleIn(
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            ),
                            initialScale = 0.95f
                        )).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutLinearInEasing
                                )
                            ) { -it / 2 } + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutLinearInEasing
                                )
                            ) + scaleOut(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = FastOutLinearInEasing
                                ),
                                targetScale = 1.05f
                            )
                        )
                    },
                    label = "status_animation"
                ) { currentWeeklyShift ->
                    if (currentWeeklyShift == null) {
                        // Nessuna pubblicazione con animazione pulsante
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "error_pulse"
                        )

                        Text(
                            text = "Nessuna pubblicazione per questa settimana",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error.copy(alpha = alpha)
                        )
                    } else {
                        // Status row con micro-animazioni
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (statusText, statusColor) = when (currentWeeklyShift.status) {
                                WeeklyShiftStatus.PUBLISHED -> "Pubblicata" to Color(0xFF4CAF50)
                                WeeklyShiftStatus.DRAFT -> "Bozza" to Color(0xFFFF9800)
                                WeeklyShiftStatus.NOT_PUBLISHED -> "In lavorazione" to Color(0xFF9E9E9E)
                            }

                            // Status indicator con animazione del colore
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val animatedColor by animateColorAsState(
                                    targetValue = statusColor,
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "status_color_animation"
                                )

                                val scale by animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    ),
                                    label = "status_dot_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(scale)
                                        .background(animatedColor, shape = CircleShape)
                                )

                                AnimatedContent(
                                    targetState = statusText,
                                    transitionSpec = {
                                        (fadeIn(
                                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                                        ) + slideInVertically(
                                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                                        ) { it / 4 }).togetherWith(
                                            fadeOut(
                                                animationSpec = tween(200, easing = FastOutLinearInEasing)
                                            ) + slideOutVertically(
                                                animationSpec = tween(200, easing = FastOutLinearInEasing)
                                            ) { -it / 4 }
                                        )
                                    },
                                    label = "status_text_animation"
                                ) { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Creation date con slide animation
                            AnimatedContent(
                                targetState = currentWeeklyShift.createdAt,
                                transitionSpec = {
                                    (slideInHorizontally(
                                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                                    ) { it / 2 } + fadeIn(
                                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(300, easing = FastOutLinearInEasing)
                                        ) { -it / 2 } + fadeOut(
                                            animationSpec = tween(300, easing = FastOutLinearInEasing)
                                        )
                                    )
                                },
                                label = "creation_date_animation"
                            ) { createdAt ->
                                Text(
                                    text = "Creata: ${createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yy"))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}