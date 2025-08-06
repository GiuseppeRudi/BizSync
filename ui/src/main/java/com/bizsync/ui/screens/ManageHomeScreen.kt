package com.bizsync.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.UrgencyLevel
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.DipartimentoInfo
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.TimbratureWithUser
import com.bizsync.domain.model.TodayStats
import com.bizsync.domain.model.TurnoWithUsers
import com.bizsync.domain.model.User
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.UserState
import com.bizsync.ui.viewmodels.ManagerHomeViewModel
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerHomeScreen(
    userState: UserState,
    viewModel: ManagerHomeViewModel = hiltViewModel(),
) {
    val homeState by viewModel.homeState.collectAsState()
    val currentTime = remember {
        mutableStateOf(LocalDateTime.now())
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // 1 minuto
            currentTime.value = LocalDateTime.now()
        }
    }


    LaunchedEffect(Unit) {
        viewModel.loadHomeManagerData(userState.azienda.toDomain())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con informazioni giornata
        item {
            WelcomeHeader(
                user = userState.user.toDomain(),
                azienda = userState.azienda.toDomain(),
                currentTime = currentTime.value
            )
        }

        // Alert pubblicazione turni
        item {
            if(homeState.shiftsPublishedThisWeek == false) {
                ShiftPublicationAlert(
                    daysUntilPublication = homeState.daysUntilShiftPublication,
                )
            }

        }

        // Statistiche giornaliere
        item {
            TodayStatsSection(
                todayStats = homeState.todayStats,
                isLoading = homeState.isLoading
            )
        }


        // Ultime timbrature
        item {
            RecentTimbratureSection(
                recentTimbrature = homeState.recentTimbrature,
            )
        }

    }
}

@Composable
fun WelcomeHeader(
    user: User,
    azienda: Azienda,
    currentTime: LocalDateTime
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ITALIAN)

    val greeting = when (currentTime.hour) {
        in 5..11 -> "Buongiorno"
        in 12..17 -> "Buon pomeriggio"
        else -> "Buonasera"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting, ${user.nome}!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = azienda.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Operativo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ShiftPublicationAlert(
    daysUntilPublication: Int,
) {

    val urgencyLevel = when {
        daysUntilPublication <= 0 -> UrgencyLevel.CRITICAL
        daysUntilPublication == 1 -> UrgencyLevel.HIGH
        daysUntilPublication <= 2 -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }

    val colors = when (urgencyLevel) {
        UrgencyLevel.CRITICAL -> Triple(
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            Color.White
        )
        UrgencyLevel.HIGH -> Triple(
            Color(0xFFFF9800),
            Color(0xFFFFF3E0),
            Color(0xFF6D4C41)
        )
        UrgencyLevel.MEDIUM -> Triple(
            Color(0xFFFFC107),
            Color(0xFFFFFDE7),
            Color(0xFF5D4037)
        )
        UrgencyLevel.LOW -> Triple(
            Color(0xFF2196F3),
            Color(0xFFE3F2FD),
            Color(0xFF1565C0)
        )
    }

    val message = when {
        daysUntilPublication < 0 -> "⚠️ I turni dovevano essere pubblicati ${-daysUntilPublication} giorni fa!"
        daysUntilPublication == 0 -> "🚨 I turni devono essere pubblicati OGGI!"
        daysUntilPublication == 1 -> "⏰ I turni devono essere pubblicati DOMANI"
        else -> "📅 $daysUntilPublication giorni alla pubblicazione dei turni"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.second
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.first
                    )

                    if (urgencyLevel == UrgencyLevel.CRITICAL) {
                        Text(
                            text = "Azione richiesta immediatamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.first,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (urgencyLevel == UrgencyLevel.CRITICAL) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.first
                    )
                }
            }

        }
    }
}

@Composable
fun TodayStatsSection(
    todayStats: TodayStats,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "Situazione di oggi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatCard(
                            title = "Turni Assegnati",
                            value = todayStats.turniTotaliAssegnati.toString(),
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            color = Color(0xFF2196F3),
                            subtitle = "Totali oggi"
                        )
                    }
                    item {
                        StatCard(
                            title = "Utenti Attivi",
                            value = todayStats.utentiAttiviOggi.toString(),
                            icon = Icons.Default.People,
                            color = Color(0xFF4CAF50),
                            subtitle = "Con turni"
                        )
                    }
                    item {
                        StatCard(
                            title = "Dipartimenti",
                            value = todayStats.dipartimentiAperti.toString(),
                            icon = Icons.Default.Business,
                            color = Color(0xFFFF9800),
                            subtitle = "Aperti oggi"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stato Turni",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatCard(
                            title = "Completati",
                            value = todayStats.turniCompletati.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            subtitle = "Entrata + Uscita"
                        )
                    }
                    item {
                        StatCard(
                            title = "In Corso",
                            value = todayStats.turniIniziati.toString(),
                            icon = Icons.Default.Schedule,
                            color = Color(0xFF2196F3),
                            subtitle = "Solo entrata"
                        )
                    }
                    item {
                        StatCard(
                            title = "Da Iniziare",
                            value = todayStats.turniDaIniziare.toString(),
                            icon = Icons.Default.HourglassEmpty,
                            color = Color(0xFFFF9800),
                            subtitle = "Nessuna timbratura"
                        )
                    }
                }

                if (todayStats.dipartimentiDetails.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dipartimenti Attivi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        todayStats.dipartimentiDetails.forEach { dipartimento ->
                            DipartimentoCard(dipartimento = dipartimento)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DipartimentoCard(
    dipartimento: DipartimentoInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = dipartimento.nome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${dipartimento.orarioApertura} - ${dipartimento.orarioChiusura}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Badge(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${dipartimento.numeroTurni}",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(101.dp).height(135.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}


@Composable
fun RecentTimbratureSection(
    recentTimbrature: List<TimbratureWithUser>, ) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "Ultime Timbrature",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recentTimbrature.isEmpty()) {
                Text(
                    text = "Nessuna timbratura recente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                recentTimbrature.take(5).forEach { timbratureWithUser ->
                    TimbratureItem(
                        timbrature = timbratureWithUser.timbrature,
                        user = timbratureWithUser.user
                    )

                    if (timbratureWithUser != recentTimbrature.take(5).last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimbratureItem(
    timbrature: Timbratura,
    user: User
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
            Avatar(
                name = "${user.nome} ${user.cognome}",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${user.nome} ${user.cognome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = user.dipartimento,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = timbrature.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            TimbratureTypeChip(type = timbrature.tipoTimbratura)
        }
    }
}

@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .take(2).joinToString("") { it.first().uppercase() }

    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TimbratureTypeChip(type: TipoTimbratura) {
    val (color, text) = when (type) {
        TipoTimbratura.ENTRATA -> Color(0xFF4CAF50) to "Entrata"
        TipoTimbratura.USCITA -> Color(0xFFF44336) to "Uscita"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TodayShiftsSection(
    todayShifts: List<TurnoWithUsers>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "Turni di Oggi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onViewAll) {
                    Text("Vedi tutti")
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (todayShifts.isEmpty()) {
                Text(
                    text = "Nessun turno programmato per oggi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                todayShifts.take(3).forEach { turnoWithUsers ->
                    TurnoItem(turnoWithUsers = turnoWithUsers)

                    if (turnoWithUsers != todayShifts.take(3).last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TurnoItem(turnoWithUsers: TurnoWithUsers) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = turnoWithUsers.turno.titolo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${
                            turnoWithUsers.turno.orarioInizio.format(
                                DateTimeFormatter.ofPattern(
                                    "HH:mm"
                                )
                            )
                        } - ${turnoWithUsers.turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Badge(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${turnoWithUsers.users.size}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (turnoWithUsers.users.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = turnoWithUsers.users.take(2)
                        .joinToString(", ") { "${it.nome} ${it.cognome}" } +
                            if (turnoWithUsers.users.size > 2) " e altri ${turnoWithUsers.users.size - 2}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}