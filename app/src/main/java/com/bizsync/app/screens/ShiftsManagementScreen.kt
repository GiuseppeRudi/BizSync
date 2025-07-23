package com.bizsync.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.viewmodels.CompletenessaTurno
import com.bizsync.ui.viewmodels.ShiftTimeFilter
import com.bizsync.ui.viewmodels.ShiftsManagementViewModel
import com.bizsync.ui.viewmodels.StatoTurnoDettagliato
import com.bizsync.ui.viewmodels.TurnoWithTimbratureDetails
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ShiftsManagementScreen(
    viewModel: ShiftsManagementViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val userVM = LocalUserViewModel.current

    val userState by userVM.uiState.collectAsState()

    val user = userState.user.toDomain()
    val azienda = userState.azienda.toDomain()

    // Inizializza lo screen
    LaunchedEffect(user, azienda) {
        viewModel.initializeScreen(user, azienda)
    }

    // Gestione errori
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Mostra errore per qualche secondo poi lo rimuove
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "I Miei Turni",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Aggiorna",
                            modifier = if (uiState.isLoading) {
                                Modifier.rotate(
                                    animateFloatAsState(
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        )
                                    ).value
                                )
                            } else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sezione filtri
                FilterSection(
                    selectedFilter = uiState.selectedFilter,
                    onFilterChange = { viewModel.changeFilter(it) },
                    turniCount = uiState.turniWithTimbrature.size
                )

                // Lista turni
                if (uiState.turniWithTimbrature.isEmpty() && !uiState.isLoading) {
                    EmptyStateSection()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.turniWithTimbrature) { turnoWithDetails ->
                            TurnoCard(
                                turnoWithDetails = turnoWithDetails,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Spazio aggiuntivo in fondo
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Caricamento turni...",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Snackbar per errori
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedFilter: ShiftTimeFilter,
    onFilterChange: (ShiftTimeFilter) -> Unit,
    turniCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Periodo di visualizzazione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Badge(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$turniCount turni",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ShiftTimeFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = if (selectedFilter == filter) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun TurnoCard(
    turnoWithDetails: TurnoWithTimbratureDetails,
    modifier: Modifier = Modifier
) {
    val turno = turnoWithDetails.turno
    val entrata = turnoWithDetails.timbratureEntrata
    val uscita = turnoWithDetails.timbratureUscita

    // Colori basati sullo stato del turno
    val (cardColor, statusColor, statusIcon) = getCardColors(turnoWithDetails.statoTurno)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del turno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = turno.data.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ITALIAN))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = turno.titolo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Badge stato
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getStatusText(turnoWithDetails.statoTurno),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orari previsti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Orario Previsto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Durata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${turno.calcolaDurata()}h",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timbrature effettive
            if (turnoWithDetails.completezza != CompletenessaTurno.NON_RICHIESTO) {
                TimbratureSection(
                    entrata = entrata,
                    uscita = uscita,
                    turno = turno,
                    minutiRitardoEntrata = turnoWithDetails.minutiRitardoEntrata,
                    minutiRitardoUscita = turnoWithDetails.minutiRitardoUscita,
                    minutiLavoratiEffettivi = turnoWithDetails.minutiLavoratiEffettivi
                )
            } else {
                // Turno futuro
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "⏱️ Turno programmato",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Zona lavorativa
            Spacer(modifier = Modifier.height(12.dp))

            val zonaLavorativa = turno.getZonaLavorativaDipendente(entrata?.idDipendente ?: "")
            val (zonaIcon, zonaText, zonaColor) = when (zonaLavorativa) {
                ZonaLavorativa.IN_SEDE -> Triple(Icons.Default.Business, "In Sede", Color(0xFF2196F3))
                ZonaLavorativa.SMART_WORKING -> Triple(Icons.Default.Home, "Smart Working", Color(0xFF4CAF50))
                ZonaLavorativa.TRASFERTA -> Triple(Icons.Default.Flight, "Trasferta", Color(0xFF9C27B0))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = zonaIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = zonaColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = zonaText,
                    style = MaterialTheme.typography.bodySmall,
                    color = zonaColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TimbratureSection(
    entrata: Timbratura?,
    uscita: Timbratura?,
    turno: Turno,
    minutiRitardoEntrata: Int,
    minutiRitardoUscita: Int,
    minutiLavoratiEffettivi: Int
) {
    Column {
        // Sezione entrata
        TimbratureRow(
            tipo = "Entrata",
            timbratura = entrata,
            orarioPrevisto = turno.orarioInizio,
            minutiRitardo = minutiRitardoEntrata,
            icon = Icons.AutoMirrored.Filled.Login
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sezione uscita
        TimbratureRow(
            tipo = "Uscita",
            timbratura = uscita,
            orarioPrevisto = turno.orarioFine,
            minutiRitardo = minutiRitardoUscita,
            icon = Icons.AutoMirrored.Filled.Logout
        )

        // Statistiche lavorative
        if (entrata != null && uscita != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ore Lavorate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${minutiLavoratiEffettivi / 60}h ${minutiLavoratiEffettivi % 60}m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Efficienza",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val orePreviste = turno.calcolaDurata() * 60
                        val percentuale = if (orePreviste > 0) {
                            (minutiLavoratiEffettivi * 100) / orePreviste
                        } else 0
                        Text(
                            text = "$percentuale%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                percentuale >= 95 -> Color(0xFF4CAF50)
                                percentuale >= 80 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimbratureRow(
    tipo: String,
    timbratura: Timbratura?,
    orarioPrevisto: LocalTime,
    minutiRitardo: Int,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (timbratura != null) {
                    when (tipo) {
                        "Entrata" -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                    }
                } else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = tipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (timbratura != null) {
                        timbratura.dataOraTimbratura.format(DateTimeFormatter.ofPattern("HH:mm"))
                    } else {
                        "Non timbrata"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timbratura != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else Color(0xFF9E9E9E)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Previsto: ${orarioPrevisto.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (minutiRitardo > 0) {
                Text(
                    text = "⏰ +${minutiRitardo}min",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            } else if (timbratura != null) {
                Text(
                    text = "✓ Puntuale",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nessun turno trovato",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Prova a modificare i filtri o contatta il manager",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }
    }
}

// Funzioni helper per colori e testi
private fun getCardColors(stato: StatoTurnoDettagliato): Triple<Color, Color, ImageVector> {
    return when (stato) {
        StatoTurnoDettagliato.COMPLETATO_REGOLARE -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
        StatoTurnoDettagliato.COMPLETATO_RITARDO -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            Icons.Default.Schedule
        )
        StatoTurnoDettagliato.COMPLETATO_ANTICIPO -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3),
            Icons.Default.FastForward
        )
        StatoTurnoDettagliato.PARZIALE_SOLO_ENTRATA -> Triple(
            Color(0xFFFFC107).copy(alpha = 0.1f),
            Color(0xFFFFC107),
            Icons.Default.Warning
        )
        StatoTurnoDettagliato.ASSENTE -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Color(0xFFF44336),
            Icons.Default.Cancel
        )
        StatoTurnoDettagliato.TURNO_FUTURO -> Triple(
            Color(0xFF9E9E9E).copy(alpha = 0.1f),
            Color(0xFF9E9E9E),
            Icons.Default.Schedule
        )
        StatoTurnoDettagliato.NON_INIZIATO -> Triple(
            Color(0xFF9E9E9E).copy(alpha = 0.1f),
            Color(0xFF9E9E9E),
            Icons.Default.RadioButtonUnchecked
        )
    }
}

private fun getStatusText(stato: StatoTurnoDettagliato): String {
    return when (stato) {
        StatoTurnoDettagliato.COMPLETATO_REGOLARE -> "Completato"
        StatoTurnoDettagliato.COMPLETATO_RITARDO -> "Ritardo"
        StatoTurnoDettagliato.COMPLETATO_ANTICIPO -> "Anticipo"
        StatoTurnoDettagliato.PARZIALE_SOLO_ENTRATA -> "Parziale"
        StatoTurnoDettagliato.ASSENTE -> "Assente"
        StatoTurnoDettagliato.TURNO_FUTURO -> "Programmato"
        StatoTurnoDettagliato.NON_INIZIATO -> "Non iniziato"
    }
}