package com.bizsync.app.screens


import android.net.http.UrlRequest
import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.model.domain.Invito
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.viewmodels.InvitiViewModel


@Preview(showBackground = true, name = "Scaffold")
@Composable
private fun InvitiPreview() {

    ChooseInvito(onTerminate = {})
}


@Composable
fun ChooseInvito(onTerminate: () -> Unit) {
    val invitiVM: InvitiViewModel = hiltViewModel()
    val invites by invitiVM.invites.collectAsState()
    val loading by invitiVM.isLoading.collectAsState()
    val erroreStatus by invitiVM.errorStatusInvite.collectAsState()
    val errorMessage by invitiVM.errorMessageStatusInvite.collectAsState()
    val userVM = LocalUserViewModel.current
    val user by userVM.user.collectAsState()



    LaunchedEffect(user) {
        user.let {
            Log.d("INVITI_DEBUG",  "EMAIL: " + user?.email.toString())

            invitiVM.fetchInvites(user?.email.toString())   // ora che ho l'uid, chiamo la fetch vera
        }

    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Loading indicator
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Caricamento inviti...", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Invite list
        AnimatedVisibility(
            visible = !loading,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            if (invites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Nessun invito disponibile", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ciclo tramite i dati (invites)
                    items(invites.size) { index ->
                        val invite = invites[index]
                        InviteCard(
                            invite = invite,
                            onAccept = { invitiVM.acceptInvite(invite, userVM)
                                          onTerminate()},
                            onDetails = { invitiVM.showDetails(invite) },
                            onDecline = { invitiVM.declineInvite(invite) }
                        )
                    }
                }
            }
        }
    }

    StatusDialog(message = errorMessage, statusType = erroreStatus, onDismiss = { invitiVM.clearErrorMessage()} )
}





@Composable
private fun InviteCard(
    invite: Invito,
    onAccept: () -> Unit,
    onDetails: () -> Unit,
    onDecline: () -> Unit
) {
    var expanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            //.animateItemPlacement()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Azienda: ${invite.aziendaNome}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Manageriale : ${invite.manager}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Ruolo: ${invite.nomeRuolo}", style = MaterialTheme.typography.bodyMedium)

            // Extra content
            AnimatedVisibility(visible = expanded.value) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                 //   Text(text = "Data offerta: ${invite.date}")
                 //   Text(text = "Posizione: ${invite.position}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onAccept) { Text("Accetta") }
                Button(onClick = onDetails) { Text("Dettagli") }
                Button(onClick = onDecline) { Text("Rifiuta") }
            }
        }
    }
}









