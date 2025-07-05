package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.viewmodels.ManageInviteViewModel
import com.bizsync.domain.constants.enumClass.InviteView
import com.bizsync.ui.components.StatusDialog
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.viewmodels.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteManagementScreen(
    companyVM: CompanyViewModel
) {

    val scaffoldVm = LocalScaffoldViewModel.current

    LaunchedEffect(Unit) {
        scaffoldVm.onFullScreenChanged(false)
    }

    val userVM = LocalUserViewModel.current
    val inviteVM: ManageInviteViewModel = hiltViewModel()
    val inviteState by inviteVM.uiState.collectAsState()
    val userState by userVM.uiState.collectAsState()

    val azienda = userState.azienda

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestione Inviti",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { companyVM.setSelectedOperation(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        // FIXED: Removed verticalScroll() from the outer Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            // .verticalScroll(rememberScrollState()) // ← REMOVED THIS LINE
        ) {
            when (inviteState.currentView) {
                InviteView.SELECTION -> {
                    SelectionContent(
                        onViewInvites = { inviteVM.setCurrentView(InviteView.VIEW_INVITES) },
                        onCreateInvite = { inviteVM.setCurrentView(InviteView.CREATE_INVITE) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                InviteView.VIEW_INVITES -> {
                    ViewInvitesContent(
                        inviteVM = inviteVM,
                        Idazienda = azienda.idAzienda,
                        onBack = { inviteVM.setCurrentView(InviteView.SELECTION) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                InviteView.CREATE_INVITE -> {
                    CreateInviteContent(
                        inviteVM = inviteVM,
                        azienda = azienda,
                        onBack = { inviteVM.setCurrentView(InviteView.SELECTION) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

            }
        }
    }

    // Status dialog per risultato
    if (inviteState.resultMessage != null) {
        StatusDialog(
            message = inviteState.resultMessage,
            statusType = inviteState.resultStatus,
            onDismiss = { inviteVM.clearResult() }
        )
    }
}

@Composable
fun SelectionContent(
    onViewInvites: () -> Unit,
    onCreateInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    // FIXED: Added verticalScroll here since this content doesn't have LazyColumn
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // ← Moved verticalScroll here
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cosa vuoi fare?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Card per visualizzare inviti
        Card(
            onClick = onViewInvites,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Visualizza Inviti",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vedi tutti gli inviti inviati e il loro stato",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Card(
            onClick = onCreateInvite,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Nuovo Invito",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crea e invia un nuovo invito",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ViewInvitesContent(
    inviteVM: ManageInviteViewModel,
    Idazienda: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inviteState by inviteVM.uiState.collectAsState()

    LaunchedEffect(Unit) {
        inviteVM.loadInvites(Idazienda)
    }

    Column(modifier = modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
            }
            Text(
                text = "Inviti Inviati",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista inviti
        if (inviteState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (inviteState.invites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Nessun invito inviato",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // FIXED: LazyColumn now has proper constraints
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(inviteState.invites) { invite ->
                    InviteItem(invite = invite)
                }
            }
        }
    }
}

@Composable
fun InviteItem(
    invite: InvitoUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invite.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Status badge
                StatusBadge(status = invite.stato)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inviato: ${invite.sentDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (invite.acceptedDate != null) {
                Text(
                    text = "Accettato: ${invite.acceptedDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: StatusInvite,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        StatusInvite.PENDING -> "In attesa" to MaterialTheme.colorScheme.primary
        StatusInvite.ACCEPTED -> "Accettato" to Color.Green
        StatusInvite.REJECTED -> "Rifiutato" to Color.Red
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}