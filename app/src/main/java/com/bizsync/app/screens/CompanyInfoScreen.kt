package com.bizsync.app.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.theme.BizSyncColors
import com.bizsync.ui.theme.BizSyncDimensions
import java.time.DayOfWeek
import androidx.compose.runtime.getValue
import com.bizsync.ui.mapper.toDomain

@Composable
fun CompanyInfoScreen(
    onBackClick: () -> Unit
) {

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val contratto = userState.contratto
    val azienda = userState.azienda.toDomain()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = BizSyncColors.OnBackground
                )
            }

            Text(
                text = "Informazioni Azienda",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BizSyncColors.OnBackground
                ),
                modifier = Modifier.padding(start = BizSyncDimensions.SpacingSmall)
            )
        }

        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingMedium))

        // Contenuto scorrevole
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingMedium)
        ) {
            // Informazioni Azienda
            item {
                CompanyInfoCard(azienda = azienda)
            }

            // Informazioni Contratto
            item {
                ContractInfoCard(contratto = contratto)
            }

            // Informazioni CCNL
            item {
                CcnlInfoCard(ccnlInfo = contratto.ccnlInfo)
            }

            // Utilizzo Benefici
            item {
                BenefitsUsageCard(contratto = contratto)
            }

        }
    }
}

@Composable
fun CompanyInfoCard(azienda: Azienda) {
    InfoCard(
        title = "Informazioni Azienda",
        icon = Icons.Default.Business
    ) {
        InfoRow(
            label = "Nome Azienda",
            value = azienda.nome,
            icon = Icons.Default.Business
        )

        InfoRow(
            label = "Settore",
            value = azienda.sector,
            icon = Icons.Default.Category
        )

        InfoRow(
            label = "Dimensioni",
            value = "${azienda.numDipendentiRange} dipendenti",
            icon = Icons.Default.People
        )

        InfoRow(
            label = "Pubblicazione Turni",
            value = getDayOfWeekInItalian(azienda.giornoPubblicazioneTurni),
            icon = Icons.Default.Schedule
        )

        // Aree di Lavoro
        if (azienda.areeLavoro.isNotEmpty()) {
            Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingSmall))

            Text(
                text = "Aree di Lavoro",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BizSyncColors.OnSurface
                ),
                modifier = Modifier.padding(vertical = BizSyncDimensions.SpacingSmall)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingSmall)
            ) {
                items(azienda.areeLavoro) { area ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = BizSyncColors.Primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = area.nomeArea, // Assumendo che AreaLavoro abbia un campo nome
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BizSyncColors.Primary
                            ),
                            modifier = Modifier.padding(
                                horizontal = BizSyncDimensions.SpacingSmall,
                                vertical = 4.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContractInfoCard(contratto: Contratto) {
    InfoCard(
        title = "Informazioni Contratto",
        icon = Icons.Default.Description
    ) {
        InfoRow(
            label = "Tipo Contratto",
            value = contratto.tipoContratto,
            icon = Icons.Default.Work
        )

        InfoRow(
            label = "Ore Settimanali",
            value = "${contratto.oreSettimanali} ore",
            icon = Icons.Default.Schedule
        )

        InfoRow(
            label = "Data Inizio",
            value = formatDate(contratto.dataInizio),
            icon = Icons.Default.CalendarToday
        )

        InfoRow(
            label = "Dipartimento",
            value = contratto.dipartimento,
            icon = Icons.Default.Domain
        )

        InfoRow(
            label = "Posizione",
            value = contratto.posizioneLavorativa,
            icon = Icons.Default.Badge
        )

        InfoRow(
            label = "Settore Aziendale",
            value = contratto.settoreAziendale,
            icon = Icons.Default.Category
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CcnlInfoCard(ccnlInfo: Ccnlnfo) {
    InfoCard(
        title = "Informazioni CCNL",
        icon = Icons.Default.Gavel
    ) {
        InfoRow(
            label = "Settore CCNL",
            value = ccnlInfo.settore,
            icon = Icons.Default.Category
        )

        InfoRow(
            label = "Ruolo",
            value = ccnlInfo.ruolo,
            icon = Icons.Default.Person
        )

        InfoRow(
            label = "Stipendio Annuale Lordo",
            value = "€ ${String.format("%,d", ccnlInfo.stipendioAnnualeLordo)}",
            icon = Icons.Default.Euro
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ferie
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = BizSyncColors.Primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(BizSyncDimensions.SpacingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BeachAccess,
                            contentDescription = null,
                            tint = BizSyncColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${ccnlInfo.ferieAnnue}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BizSyncColors.Primary
                            )
                        )
                        Text(
                            text = "Giorni Ferie",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BizSyncColors.OnSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingSmall))

            // ROL
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = BizSyncColors.Secondary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(BizSyncDimensions.SpacingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = BizSyncColors.Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${ccnlInfo.rolAnnui}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BizSyncColors.Secondary
                            )
                        )
                        Text(
                            text = "Ore ROL",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BizSyncColors.OnSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingSmall))

        InfoRow(
            label = "Malattia Retribuita",
            value = "${ccnlInfo.malattiaRetribuita} giorni",
            icon = Icons.Default.LocalHospital
        )
    }
}

@Composable
fun BenefitsUsageCard(contratto: Contratto) {
    InfoCard(
        title = "Utilizzo Benefici",
        icon = Icons.Default.Timeline
    ) {
        val ccnl = contratto.ccnlInfo

        // Ferie utilizzate
        BenefitUsageRow(
            title = "Ferie Utilizzate",
            used = contratto.ferieUsate,
            total = ccnl.ferieAnnue,
            unit = "giorni",
            icon = Icons.Default.BeachAccess,
            color = BizSyncColors.Primary
        )

        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingSmall))

        // ROL utilizzate
        BenefitUsageRow(
            title = "ROL Utilizzate",
            used = contratto.rolUsate,
            total = ccnl.rolAnnui,
            unit = "ore",
            icon = Icons.Default.AccessTime,
            color = BizSyncColors.Secondary
        )

        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingSmall))

        // Malattia utilizzata
        BenefitUsageRow(
            title = "Malattia Utilizzata",
            used = contratto.malattiaUsata,
            total = ccnl.malattiaRetribuita,
            unit = "giorni",
            icon = Icons.Default.LocalHospital,
            color = Color(0xFFFF6B6B)
        )
    }
}

@Composable
fun BenefitUsageRow(
    title: String,
    used: Int,
    total: Int,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingSmall))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BizSyncColors.OnSurface
                    )
                )
            }

            Text(
                text = "$used / $total $unit",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
        progress = { if (total > 0) used.toFloat() / total.toFloat() else 0f },
        modifier = Modifier.fillMaxWidth(),
        color = color,
        trackColor = color.copy(alpha = 0.2f),
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}


@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BizSyncColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(BizSyncDimensions.SpacingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = BizSyncDimensions.SpacingMedium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BizSyncColors.Primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingSmall))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BizSyncColors.OnSurface
                    )
                )
            }

            content()
        }
    }
}



// Funzioni di supporto
fun getDayOfWeekInItalian(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Lunedì"
        DayOfWeek.TUESDAY -> "Martedì"
        DayOfWeek.WEDNESDAY -> "Mercoledì"
        DayOfWeek.THURSDAY -> "Giovedì"
        DayOfWeek.FRIDAY -> "Venerdì"
        DayOfWeek.SATURDAY -> "Sabato"
        DayOfWeek.SUNDAY -> "Domenica"
    }
}

fun formatDate(dateString: String): String {
    // Implementa la formattazione della data secondo le tue necessità
    return dateString // Placeholder
}
