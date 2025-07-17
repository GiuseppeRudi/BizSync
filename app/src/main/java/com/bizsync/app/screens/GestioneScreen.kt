package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.bizsync.app.navigation.GestioneNavigator
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.ManagementTemplate
import com.bizsync.ui.model.ManagementCard
import com.bizsync.ui.theme.BizSyncColors
import com.bizsync.ui.theme.BizSyncDimensions

@Composable
fun GestioneScreen() {
    GestioneNavigator()
}

@Composable
fun MainManagementScreen(
    // Manager navigation functions
    onNavigateToEmployees: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToRequest: () -> Unit,
    onNavigateToManageCompany: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    // Employee navigation functions
    onNavigateToShifts: () -> Unit = {},
    onNavigateToAbsences: () -> Unit = {},
    onNavigateToActivities: () -> Unit = {},
    onNavigateToEmployeeSettings: () -> Unit = {},
    onNavigateToEmployeeFinance: () -> Unit = {},
    onNavigateToCompanyInfo: () -> Unit = {}
) {
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val isManager = userState.user.isManager

    // Cards per Manager
    val managerCards = remember {
        listOf(
            ManagementCard(
                title = "Dipendenti",
                description = "Gestisci il personale",
                icon = Icons.Default.People,
                gradient = BizSyncColors.CardGradients[0]
            ) { onNavigateToEmployees() },

            ManagementCard(
                title = "Progetti",
                description = "Monitora progetti attivi",
                icon = Icons.AutoMirrored.Filled.Assignment,
                gradient = BizSyncColors.CardGradients[1]
            ) { onNavigateToProjects() },

            ManagementCard(
                title = "Finanze",
                description = "Controlla budget e fatture",
                icon = Icons.Default.AccountBalance,
                gradient = BizSyncColors.CardGradients[2]
            ) { onNavigateToFinance() },

            ManagementCard(
                title = "Richieste",
                description = "Gestione permessi e ferie",
                icon = Icons.Default.Inventory,
                gradient = BizSyncColors.CardGradients[3]
            ) { onNavigateToRequest() },

            ManagementCard(
                title = "Azienda",
                description = "Gestione Azienda",
                icon = Icons.Default.ContactPhone,
                gradient = BizSyncColors.CardGradients[4]
            ) { onNavigateToManageCompany() },

            ManagementCard(
                title = "Reportistica",
                description = "Analytics e report",
                icon = Icons.Default.Analytics,
                gradient = BizSyncColors.CardGradients[5]
            ) { onNavigateToReports() },

            ManagementCard(
                title = "Impostazioni",
                description = "Configurazioni sistema",
                icon = Icons.Default.Settings,
                gradient = BizSyncColors.CardGradients[6]
            ) { onNavigateToSettings() },

            ManagementCard(
                title = "Sicurezza",
                description = "Gestisci permessi e sicurezza",
                icon = Icons.Default.Security,
                gradient = BizSyncColors.CardGradients[7]
            ) { onNavigateToSecurity() }
        )
    }

    // Cards per Dipendenti
    val employeeCards = remember {
        listOf(
            ManagementCard(
                title = "Turni",
                description = "Visualizza i tuoi turni",
                icon = Icons.Default.Schedule,
                gradient = BizSyncColors.CardGradients[0]
            ) { onNavigateToShifts() },

            ManagementCard(
                title = "Assenze",
                description = "Richiedi permessi e ferie",
                icon = Icons.Default.EventBusy,
                gradient = BizSyncColors.CardGradients[1]
            ) { onNavigateToAbsences() },

            ManagementCard(
                title = "Attività",
                description = "Le tue attività giornaliere",
                icon = Icons.Default.Task,
                gradient = BizSyncColors.CardGradients[2]
            ) { onNavigateToActivities() },

            ManagementCard(
                title = "Impostazioni",
                description = "Profilo e preferenze",
                icon = Icons.Default.Settings,
                gradient = BizSyncColors.CardGradients[3]
            ) { onNavigateToEmployeeSettings() },

            ManagementCard(
                title = "Finanze",
                description = "Buste paga e rimborsi",
                icon = Icons.Default.Payment,
                gradient = BizSyncColors.CardGradients[4]
            ) { onNavigateToEmployeeFinance() },

            ManagementCard(
                title = "Azienda",
                description = "Informazioni aziendali",
                icon = Icons.Default.Business,
                gradient = BizSyncColors.CardGradients[5]
            ) { onNavigateToCompanyInfo() }
        )
    }

    // Seleziona le card in base al ruolo
    val displayCards = if (isManager) managerCards else employeeCards

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {
        // Header dinamico
        Text(
            text = if (isManager) "Gestione Aziendale" else "Area Dipendente",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BizSyncColors.OnBackground
            ),
            modifier = Modifier.padding(bottom = BizSyncDimensions.SpacingLarge)
        )

        // Sottotitolo con info ruolo
        Text(
            text = if (isManager)
                "Amministra la tua azienda"
            else
                "Gestisci le tue attività lavorative",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = BizSyncColors.OnSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = BizSyncDimensions.SpacingMedium)
        )

        // Grid di card
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingMedium),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayCards.chunked(2)) { cardPair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cardPair.forEach { card ->
                        ManagementCardItem(
                            card = card,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (cardPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ManagementCardItem(
    card: ManagementCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(BizSyncDimensions.CardHeight)
            .clickable { card.onClick() },
        shape = RoundedCornerShape(BizSyncDimensions.CardRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = BizSyncDimensions.CardElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = card.gradient,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(300f, 300f)
                    )
                )
                .padding(BizSyncDimensions.CardPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = Color.White,
                    modifier = Modifier.size(BizSyncDimensions.IconLarge)
                )

                Column {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingXSmall))

                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}

// Schermate per Dipendenti
@Composable
fun ShiftsManagementScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Turni di Lavoro",
        onBackClick = onBackClick,
        content = "Visualizza e gestisci i tuoi turni - Coming Soon"
    )
}


@Composable
fun ActivitiesManagementScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Le Tue Attività",
        onBackClick = onBackClick,
        content = "Gestisci le tue attività quotidiane - Coming Soon"
    )
}

@Composable
fun EmployeeSettingsScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Impostazioni Profilo",
        onBackClick = onBackClick,
        content = "Modifica il tuo profilo e preferenze - Coming Soon"
    )
}

@Composable
fun EmployeeFinanceScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Le Tue Finanze",
        onBackClick = onBackClick,
        content = "Buste paga e rimborsi - Coming Soon"
    )
}


// Schermate Manager esistenti
@Composable
fun ProjectsManagementScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Gestione Progetti",
        onBackClick = onBackClick,
        content = "Sezione Progetti - Coming Soon"
    )
}

@Composable
fun FinanceManagementScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Gestione Finanze",
        onBackClick = onBackClick,
        content = "Sezione Finanze - Coming Soon"
    )
}
//@Composable
//fun SettingsManagementScreen(onBackClick: () -> Unit) {
//    ManagementTemplate(
//        title = "Impostazioni Sistema",
//        onBackClick = onBackClick,
//        content = "Configurazioni sistema - Coming Soon"
//    )
//}

@Composable
fun SecurityManagementScreen(onBackClick: () -> Unit) {
    ManagementTemplate(
        title = "Sicurezza",
        onBackClick = onBackClick,
        content = "Sezione Sicurezza - Coming Soon"
    )
}

