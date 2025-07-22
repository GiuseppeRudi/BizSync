package com.bizsync.app.screens


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.model.User
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.viewmodels.EditableUserFields
import com.bizsync.ui.viewmodels.EmployeeSettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSettingsScreen(
    viewModel: EmployeeSettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val userVm = LocalUserViewModel.current
    val userState = userVm.uiState.collectAsState()

    val user = userState.value.user.toDomain()

    // Inizializza con l'utente passato
    LaunchedEffect(user) {
        viewModel.initializeWithUser(user)
    }

    // Gestione messaggi di successo
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.isSaving) {
        if (uiState.isSaving) {
            userVm.aggiornaUser(uiState.editableFields)
        }
    }

    // Gestione back press con modifiche non salvate
    BackHandler(enabled = uiState.hasUnsavedChanges && !uiState.showConfirmDialog) {
        viewModel.showConfirmDialog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Impostazioni Profilo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Informazioni personali",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges) {
                                viewModel.showConfirmDialog()
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    // Indicatore modifiche non salvate
                    if (uiState.hasUnsavedChanges) {
                        Badge(
                            containerColor = Color(0xFFFF9800),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "•",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState.hasUnsavedChanges) {
                BottomActionBar(
                    onSave = {
                        if (viewModel.validateFields()) {
                            viewModel.saveChanges()
                        }
                    },
                    onReset = { viewModel.resetChanges() },
                    isSaving = uiState.isSaving
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sezione Informazioni Account
                item {
                    AccountInfoSection(user = uiState.originalUser)
                }

                // Sezione Informazioni Aziendali
                if (uiState.originalUser.idAzienda.isNotEmpty()) {
                    item {
                        CompanyInfoSection(user = uiState.originalUser)
                    }
                }

                // Sezione Informazioni Personali (Modificabili)
                item {
                    PersonalInfoSection(
                        editableFields = uiState.editableFields,
                        onNumeroTelefonoChange = viewModel::updateNumeroTelefono,
                        onIndirizzoChange = viewModel::updateIndirizzo,
                        onCodiceFiscaleChange = viewModel::updateCodiceFiscale,
                        onDataNascitaChange = viewModel::updateDataNascita,
                        onLuogoNascitaChange = viewModel::updateLuogoNascita,
                        isEnabled = !uiState.isSaving
                    )
                }

                // Spazio aggiuntivo per il bottom bar
                if (uiState.hasUnsavedChanges) {
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
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
                        TextButton(onClick = viewModel::clearError) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Snackbar per successo
            uiState.successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = message,
                        color = Color.White
                    )
                }
            }

            // Loading overlay
            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
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
                                text = "Salvataggio in corso...",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Dialog conferma uscita con modifiche non salvate
        if (uiState.showConfirmDialog) {
            UnsavedChangesDialog(
                onConfirm = {
                    viewModel.hideConfirmDialog()
                    onBackClick()
                },
                onDismiss = viewModel::hideConfirmDialog,
                onSave = {
                    if (viewModel.validateFields()) {
                        viewModel.saveChanges()
                        viewModel.hideConfirmDialog()
                    }
                }
            )
        }
    }
}

@Composable
fun AccountInfoSection(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informazioni Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Non modificabile",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Foto profilo e nome
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = user.photourl.ifEmpty { "https://ui-avatars.com/api/?name=${user.nome}+${user.cognome}&background=random" },
                    contentDescription = "Foto profilo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${user.nome} ${user.cognome}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReadOnlyField(
                label = "Email",
                value = user.email,
                icon = Icons.Default.Email
            )
        }
    }
}

@Composable
fun CompanyInfoSection(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informazioni Aziendali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Gestito dal Manager",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReadOnlyField(
                label = "Posizione Lavorativa",
                value = user.posizioneLavorativa.ifEmpty { "Non specificata" },
                icon = Icons.Default.Work
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReadOnlyField(
                label = "Dipartimento",
                value = user.dipartimento.ifEmpty { "Non specificato" },
                icon = Icons.Default.Group
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReadOnlyField(
                label = "Ruolo",
                value = if (user.isManager) "Manager" else "Dipendente",
                icon = if (user.isManager) Icons.Default.AdminPanelSettings else Icons.Default.Person
            )
        }
    }
}

@Composable
fun PersonalInfoSection(
    editableFields: EditableUserFields,
    onNumeroTelefonoChange: (String) -> Unit,
    onIndirizzoChange: (String) -> Unit,
    onCodiceFiscaleChange: (String) -> Unit,
    onDataNascitaChange: (String) -> Unit,
    onLuogoNascitaChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Informazioni Personali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Modificabile",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EditableTextField(
                label = "Numero di Telefono",
                value = editableFields.numeroTelefono,
                onValueChange = onNumeroTelefonoChange,
                icon = Icons.Default.Phone,
                placeholder = "es. +39 123 456 7890",
                keyboardType = KeyboardType.Phone,
                isEnabled = isEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditableTextField(
                label = "Indirizzo",
                value = editableFields.indirizzo,
                onValueChange = onIndirizzoChange,
                icon = Icons.Default.LocationOn,
                placeholder = "es. Via Roma 123, Milano",
                maxLines = 2,
                isEnabled = isEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditableTextField(
                label = "Codice Fiscale",
                value = editableFields.codiceFiscale,
                onValueChange = onCodiceFiscaleChange,
                icon = Icons.Default.Badge,
                placeholder = "es. RSSMRA85M01H501Z",
                isEnabled = isEnabled,
                transformation = { it.uppercase() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditableTextField(
                label = "Data di Nascita",
                value = editableFields.dataNascita,
                onValueChange = onDataNascitaChange,
                icon = Icons.Default.Cake,
                placeholder = "es. 01/01/1990",
                keyboardType = KeyboardType.Number,
                isEnabled = isEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            EditableTextField(
                label = "Luogo di Nascita",
                value = editableFields.luogoNascita,
                onValueChange = onLuogoNascitaChange,
                icon = Icons.Default.Place,
                placeholder = "es. Milano (MI)",
                isEnabled = isEnabled
            )
        }
    }
}

@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EditableTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    isEnabled: Boolean = true,
    transformation: (String) -> String = { it },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(transformation(it)) },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun BottomActionBar(
    onSave: () -> Unit,
    onReset: () -> Unit,
    isSaving: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Annulla")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSaving) "Salvataggio..." else "Salva")
            }
        }
    }
}

@Composable
fun UnsavedChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFFF9800)
            )
        },
        title = {
            Text("Modifiche non salvate")
        },
        text = {
            Text("Hai delle modifiche non salvate. Cosa vuoi fare?")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onConfirm) {
                    Text("Esci senza salvare", color = Color(0xFFF44336))
                }
                Button(onClick = onSave) {
                    Text("Salva ed esci")
                }
            }
        }
    )
}