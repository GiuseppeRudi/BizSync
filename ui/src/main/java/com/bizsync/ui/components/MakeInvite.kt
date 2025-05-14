package com.bizsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.getValue
import android.util.Log
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.viewmodels.MakeInviteViewModel
import com.bizsync.ui.viewmodels.UserViewModel


@Composable
fun MakeInviteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    userVM : UserViewModel
) {
    val inviteVM : MakeInviteViewModel = hiltViewModel()

    val email by inviteVM.email.collectAsState()
    val ruolo by inviteVM.ruolo.collectAsState()
    val manager by inviteVM.manager.collectAsState()
    val resultMessage by inviteVM.resultMessage.collectAsState()
    val resultStatus by inviteVM.resultStatus.collectAsState()

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {

                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Invita un dipendente", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { inviteVM.onEmailChanged(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nuovo blocco per la scelta manageriale
                    Text("Ruolo manageriale?", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Button(
                            onClick = { inviteVM.onManagerChanged(true) },
                            enabled = !manager, // disattiva se già selezionato
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sì")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { inviteVM.onManagerChanged(false) },
                            enabled = manager, // disattiva se già selezionato
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("No")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ruolo,
                        onValueChange = { inviteVM.onRuoloChanged(it) },
                        label = { Text("Ruolo nell’azienda") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(onClick = onDismiss) {
                            Text("Annulla")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            Log.d("INVITO_DEBUG", userVM.azienda.value.toString())
                            inviteVM.inviaInvito(userVM.azienda.value)
                            onDismiss()
                        }) {
                            Text("Invia Invito")
                        }
                    }
                }
            }
        }
    }

    StatusDialog(
        message = resultMessage,
        statusType = resultStatus ?: DialogStatusType.SUCCESS, // o ERROR
        onDismiss = { inviteVM.clearResult() }
    )
}

