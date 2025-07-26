package com.bizsync.app.screens

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.viewmodels.InvitiViewModel
import com.bizsync.ui.viewmodels.UserViewModel

@Composable
fun ChooseInvito(onTerminate: () -> Unit) {
    val invitiVM: InvitiViewModel = hiltViewModel()
    val inviteState by invitiVM.uiState.collectAsState()
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val user = userState.user
    val checkAcceptInvite = userState.checkAcceptInvite

    LaunchedEffect(Unit) {

            invitiVM.fetchInvites(user.email)
    }

    LaunchedEffect(checkAcceptInvite) {
        if (checkAcceptInvite) {
            onTerminate()
        }
    }

    val loading = inviteState.isLoading
    val invites = inviteState.invites



    Box(modifier = Modifier.fillMaxSize()) {
        // Loading state
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Caricamento inviti...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Content state
        AnimatedVisibility(
            visible = !loading,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            if (invites.isEmpty()) {
                EmptyInvitesState()
            } else {
                InvitesContent(
                    invites = invites,
                    user = user,
                    invitiVM = invitiVM,
                    userVM = userVM,
                    inviteState = inviteState
                )
            }
        }
    }


}

@Composable
private fun EmptyInvitesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nessun invito disponibile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Controlla più tardi per nuove opportunità",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InvitesContent(
    invites: List<InvitoUi>,
    user: com.bizsync.ui.model.UserUi,
    invitiVM: InvitiViewModel,
    userVM: UserViewModel,
    inviteState: com.bizsync.ui.model.InvitiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "I tuoi inviti (${invites.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(invites.size) { index ->
            val invite = invites[index]
            InviteCard(
                invite = invite,
                onAccept = {
                    invitiVM.acceptInvite(invite, user.uid)
                    if (inviteState.updateInvite == true) {
                        userVM.onAcceptInvite(invite, inviteState.contratto)
                    }
                },
                onDecline = { invitiVM.declineInvite(invite) }
            )
        }
    }
}

@Composable
private fun InviteCard(
    invite: InvitoUi,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header - Azienda e Ruolo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invite.aziendaNome,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = invite.posizioneLavorativa,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (invite.manager) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Manager",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                StatusBadge(status = invite.stato)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informazioni principali del contratto
            ContractInfoSection(invite = invite)

            // Sezione CCNL
            if (invite.ccnlInfo.settore.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CCNLInfoSection(invite = invite)
            }

            // Sezione espandibile per dettagli aggiuntivi
            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dettagli aggiuntivi")
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    if (invite.dipartimento.isNotEmpty()) {
                        DetailItem(label = "Dipartimento", value = invite.dipartimento)
                    }

                    if (invite.settoreAziendale.isNotEmpty()) {
                        DetailItem(label = "Settore", value = invite.settoreAziendale)
                    }

                    if (invite.sentDate.isNotEmpty()) {
                        DetailItem(label = "Data invito", value = invite.sentDate)
                    }

                    if (invite.ccnlInfo.ferieAnnue > 0) {
                        DetailItem(label = "Ferie annue", value = "${invite.ccnlInfo.ferieAnnue} giorni")
                    }

                    if (invite.ccnlInfo.rolAnnui > 0) {
                        DetailItem(label = "ROL annui", value = "${invite.ccnlInfo.rolAnnui} giorni")
                    }

                    if (invite.ccnlInfo.malattiaRetribuita > 0) {
                        DetailItem(label = "Malattia retribuita", value = "${invite.ccnlInfo.malattiaRetribuita} giorni")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pulsanti azione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rifiuta")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accetta")
                }
            }
        }
    }
}

@Composable
private fun ContractInfoSection(invite: InvitoUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dettagli Contratto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (invite.tipoContratto.isNotEmpty()) {
                    InfoChip(
                        icon = Icons.Default.Schedule,
                        label = "Tipo",
                        value = invite.tipoContratto
                    )
                }

                if (invite.oreSettimanali.isNotEmpty()) {
                    InfoChip(
                        icon = Icons.Default.Schedule,
                        label = "Ore/sett",
                        value = "${invite.oreSettimanali}h"
                    )
                }
            }
        }
    }
}

@Composable
private fun CCNLInfoSection(invite: InvitoUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Informazioni CCNL",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (invite.ccnlInfo.settore.isNotEmpty()) {
                        Text(
                            text = "Settore: ${invite.ccnlInfo.settore}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (invite.ccnlInfo.ruolo.isNotEmpty()) {
                        Text(
                            text = "Ruolo: ${invite.ccnlInfo.ruolo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (invite.ccnlInfo.stipendioAnnualeLordo > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Euro,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${invite.ccnlInfo.stipendioAnnualeLordo}€",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "lordo/anno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusBadge(status: com.bizsync.domain.constants.enumClass.StatusInvite) {
    val (color, text) = when (status) {
        com.bizsync.domain.constants.enumClass.StatusInvite.PENDING ->
            MaterialTheme.colorScheme.tertiary to "In attesa"
        com.bizsync.domain.constants.enumClass.StatusInvite.ACCEPTED ->
            MaterialTheme.colorScheme.primary to "Accettato"
        com.bizsync.domain.constants.enumClass.StatusInvite.REJECTED ->
            MaterialTheme.colorScheme.error to "Rifiutato"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}