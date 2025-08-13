package com.bizsync.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.model.ProssimoTurno
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProssimoTurnoCard(
    modifier: Modifier = Modifier,
    prossimoTurno: ProssimoTurno,
    canTimbra: Boolean,
    isGettingLocation: Boolean = false,
    onTimbra: (TipoTimbratura, Context) -> Unit,
) {
    var currentTime by remember { mutableStateOf(prossimoTurno.getTempoMancanteFormattato()) }

    val context = LocalContext.current

    LaunchedEffect(prossimoTurno) {
        while (true) {
            currentTime = prossimoTurno.getTempoMancanteFormattato()
            delay(1000)
        }
    }

    val cardColor = when {
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.ENTRATA ->
            MaterialTheme.colorScheme.primaryContainer
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.USCITA ->
            MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when {
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.ENTRATA ->
            MaterialTheme.colorScheme.primary
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.USCITA ->
            MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon = when (prossimoTurno.tipoTimbraturaNecessaria) {
                TipoTimbratura.ENTRATA -> Icons.AutoMirrored.Filled.Login
                TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.Logout
            }

            Icon(
                imageVector = icon,
                contentDescription = "Timbratura",
                modifier = Modifier.size(48.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stato del turno
            Text(
                text = prossimoTurno.getStatoTurno(),
                style = MaterialTheme.typography.titleMedium,
                color = when (prossimoTurno.tipoTimbraturaNecessaria) {
                    TipoTimbratura.ENTRATA -> MaterialTheme.colorScheme.primary
                    TipoTimbratura.USCITA -> MaterialTheme.colorScheme.secondary
                }
            )

            prossimoTurno.turno?.let { turno ->
                Text(
                    text = turno.titolo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - " +
                            turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .height(IntrinsicSize.Min), // o un'altezza fissa, ad esempio 80.dp
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center // centra tutto il contenuto nella Card
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indicatore entrata
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (prossimoTurno.haTimbratoEntrata)
                                        Icons.Default.CheckCircle
                                    else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Entrata",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (prossimoTurno.haTimbratoEntrata)
                                        Color.Green
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Entrata",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (prossimoTurno.haTimbratoEntrata)
                                        Color.Green
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Linea di collegamento
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(40.dp)
                                    .background(
                                        if (prossimoTurno.haTimbratoEntrata)
                                            Color.Green
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        RoundedCornerShape(1.dp)
                                    )
                            )

                            // Indicatore uscita
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (prossimoTurno.haTimbratoUscita)
                                        Icons.Default.CheckCircle
                                    else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Uscita",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (prossimoTurno.haTimbratoUscita)
                                        Color.Green
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Uscita",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (prossimoTurno.haTimbratoUscita)
                                        Color.Green
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = currentTime,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { time ->
                        Text(
                            text = time,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = iconColor
                        )
                    }

                    Text(
                        text = prossimoTurno.messaggioStato,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (canTimbra) {
                Spacer(modifier = Modifier.height(20.dp))

                // Pulsante dinamico
                val buttonColor = when (prossimoTurno.tipoTimbraturaNecessaria) {
                    TipoTimbratura.ENTRATA -> MaterialTheme.colorScheme.primary
                    TipoTimbratura.USCITA -> MaterialTheme.colorScheme.secondary
                }

                Button(
                    onClick = {
                        if (!isGettingLocation) {
                            onTimbra(prossimoTurno.tipoTimbraturaNecessaria, context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isGettingLocation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGettingLocation)
                            buttonColor.copy(alpha = 0.6f)
                        else buttonColor
                    )
                ) {
                    AnimatedContent(
                        targetState = isGettingLocation,
                        transitionSpec = {
                            fadeIn(tween(300)) with fadeOut(tween(300))
                        }
                    ) { loading ->
                        if (loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "RILEVAMENTO POSIZIONE...",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (prossimoTurno.tipoTimbraturaNecessaria) {
                                        TipoTimbratura.ENTRATA -> Icons.Default.Fingerprint
                                        TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.ExitToApp
                                    },
                                    contentDescription = "Timbra",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prossimoTurno.getTestoPulsante(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isGettingLocation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Assicurati che il GPS sia attivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp), // o altra altezza a piacere
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                prossimoTurno.haTimbratoEntrata && prossimoTurno.haTimbratoUscita ->
                                    "✅ Turno completato"

                                (prossimoTurno.tempoMancante?.toMinutes() ?: 0) > 30 ->
                                    "Timbratura non ancora disponibile"

                                else -> "Finestra di timbratura non attiva"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

            }
        }
    }
}