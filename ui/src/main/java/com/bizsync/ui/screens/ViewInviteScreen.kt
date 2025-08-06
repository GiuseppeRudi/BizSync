package com.bizsync.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.viewmodels.ManageInviteViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInvitesScreen(
    inviteVM: ManageInviteViewModel,
    idAzienda: String,
    onBackClick: () -> Unit,
    onCreateInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inviteState by inviteVM.uiState.collectAsState()

    LaunchedEffect(idAzienda) {
        inviteVM.loadInvites(idAzienda)
    }

    val scaffoldVm = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
        scaffoldVm.onFullScreenChanged(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestione Inviti",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (inviteState.invites.isNotEmpty()) {
                            Text(
                                text = "${inviteState.invites.size} inviti inviati",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    // Icona per creare nuovo invito
                    IconButton(
                        onClick = onCreateInvite,
                        enabled = !inviteState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Nuovo Invito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ){ paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                inviteState.isLoading -> {
                    LoadingInvitesState()
                }

                inviteState.invites.isEmpty() -> {
                    EmptyInvitesState(onCreateInvite = onCreateInvite)
                }

                else -> {
                    InvitesListContent(
                        invites = inviteState.invites,
                        onRefresh = { inviteVM.loadInvites(idAzienda) }
                    )
                }
            }
        }
    }
}



@Composable
private fun LoadingInvitesState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "Caricamento inviti...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyInvitesState(
    onCreateInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icona principale
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            // Testo principale
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nessun invito inviato",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Inizia invitando il tuo primo dipendente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Call to action
            Button(
                onClick = onCreateInvite,
                modifier = Modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crea Primo Invito")
            }
        }
    }
}

@Composable
private fun InvitesListContent(
    invites: List<InvitoUi>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header con statistiche
        InvitesStatsCard(invites = invites)

        Spacer(modifier = Modifier.height(8.dp))

        // Lista inviti
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = invites,
                key = { it.id }
            ) { invite ->
                EnhancedInviteItem(invite = invite)
            }

            // Spazio extra per il FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}



@Composable
private fun InvitesStatsCard(
    invites: List<InvitoUi>,
    modifier: Modifier = Modifier
) {
    val pendingCount = invites.count { it.stato == StatusInvite.PENDING }
    val acceptedCount = invites.count { it.stato == StatusInvite.ACCEPTED }
    val rejectedCount = invites.count { it.stato == StatusInvite.REJECTED }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                count = pendingCount,
                label = "In Attesa",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            StatisticItem(
                count = acceptedCount,
                label = "Accettati",
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
            )

            StatisticItem(
                count = rejectedCount,
                label = "Rifiutati",
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatisticItem(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EnhancedInviteItem(
    invite: InvitoUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.15f), // nero più dolce
                shape = MaterialTheme.shapes.medium
            ),
            colors = CardDefaults.cardColors(
            containerColor = when (invite.stato) {
                StatusInvite.PENDING -> Color.Yellow.copy(alpha = 0.05f)
                StatusInvite.ACCEPTED -> Color.Green.copy(alpha = 0.05f)
                StatusInvite.REJECTED -> Color.Red.copy(alpha = 0.05f)
            }
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con email e status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invite.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (invite.posizioneLavorativa.isNotEmpty()) {
                        Text(
                            text = invite.posizioneLavorativa,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = invite.stato)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informazioni aggiuntive
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InviteInfoChip(
                    icon = Icons.Default.CalendarToday,
                    text = "Inviato: ${invite.sentDate}"
                )

                if (invite.dipartimento.isNotEmpty()) {
                    InviteInfoChip(
                        icon = Icons.Default.Business,
                        text = invite.dipartimento
                    )
                }
            }

            if (invite.acceptedDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InviteInfoChip(
                    icon = Icons.Default.CheckCircle,
                    text = "Accettato: ${invite.acceptedDate}",
                    color = Color.Black
                )
            }
        }
    }
}


@Composable
private fun InviteInfoChip(
    icon: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}



@Composable
fun StatusBadge(
    status: StatusInvite,
    modifier: Modifier = Modifier
) {
    val (text, color, icon) = when (status) {
        StatusInvite.PENDING -> Triple(
            "In attesa",
            MaterialTheme.colorScheme.primary,
            Icons.Default.Schedule
        )
        StatusInvite.ACCEPTED -> Triple(
            "Accettato",
            Color.Black,
            Icons.Default.CheckCircle
        )
        StatusInvite.REJECTED -> Triple(
            "Rifiutato",
            Color.Black,
            Icons.Default.Cancel
        )
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}