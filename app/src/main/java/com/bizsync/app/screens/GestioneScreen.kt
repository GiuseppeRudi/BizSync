package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.GestioneNavigator
import com.bizsync.ui.viewmodels.GestioneViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.model.ManagementCard
import com.bizsync.ui.viewmodels.UserViewModel
import com.bizsync.ui.theme.BizSyncColors
import com.bizsync.ui.theme.BizSyncDimensions

@Composable
fun GestioneScreen() {
    GestioneNavigator()
}

@Composable
fun MainManagementScreen(
    onNavigateToEmployees: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSecurity: () -> Unit
) {
    val userVM = LocalUserViewModel.current

    val userState by userVM.uiState.collectAsState()
    val manager = userState.user.isManager


    val managementCards = remember {
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
                title = "Inventario",
                description = "Gestisci magazzino",
                icon = Icons.Default.Inventory,
                gradient = BizSyncColors.CardGradients[3]
            ) { onNavigateToInventory() },

            ManagementCard(
                title = "Clienti",
                description = "CRM e relazioni clienti",
                icon = Icons.Default.ContactPhone,
                gradient = BizSyncColors.CardGradients[4]
            ) { onNavigateToCustomers() },

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {
        // Header
        Text(
            text = "Gestione Aziendale",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BizSyncColors.OnBackground
            ),
            modifier = Modifier.padding(bottom = BizSyncDimensions.SpacingLarge)
        )

        // Grid di card
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingMedium),
            modifier = Modifier.fillMaxSize()
        ) {
            items(managementCards.chunked(2)) { cardPair ->
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


@Composable
fun ProjectsManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Gestione Progetti",
        onBackClick = onBackClick,
        content = "Sezione Progetti - Coming Soon"
    )
}

@Composable
fun FinanceManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Gestione Finanze",
        onBackClick = onBackClick,
        content = "Sezione Finanze - Coming Soon"
    )
}

@Composable
fun InventoryManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Gestione Inventario",
        onBackClick = onBackClick,
        content = "Sezione Inventario - Coming Soon"
    )
}

@Composable
fun CustomersManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Gestione Clienti",
        onBackClick = onBackClick,
        content = "Sezione Clienti - Coming Soon"
    )
}

@Composable
fun ReportsManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Reportistica",
        onBackClick = onBackClick,
        content = "Sezione Report - Coming Soon"
    )
}

@Composable
fun SettingsManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Impostazioni",
        onBackClick = onBackClick,
        content = "Sezione Impostazioni - Coming Soon"
    )
}

@Composable
fun SecurityManagementScreen(onBackClick: () -> Unit) {
    ManagementScreenTemplate(
        title = "Sicurezza",
        onBackClick = onBackClick,
        content = "Sezione Sicurezza - Coming Soon"
    )
}

// Template riutilizzabile per le schermate
@Composable
private fun ManagementScreenTemplate(
    title: String,
    onBackClick: () -> Unit,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {
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
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BizSyncColors.OnBackground
                ),
                modifier = Modifier.padding(start = BizSyncDimensions.SpacingSmall)
            )
        }

        // Content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = BizSyncColors.OnSurfaceVariant
            )
        }
    }
}


