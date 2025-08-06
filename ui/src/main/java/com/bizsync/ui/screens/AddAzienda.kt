package com.bizsync.ui.screens

import android.annotation.SuppressLint
import android.location.Address
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.DipendentiSelector
import com.bizsync.ui.components.SettoreSelector
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.viewmodels.AddAziendaViewModel

@Composable
fun AddAzienda(
    onLogout: () -> Unit,
    onTerminate: () -> Unit) {
    val addaziendaviewmodel: AddAziendaViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current

    val userState by userviewmodel.uiState.collectAsState()
    val addAziendaState by addaziendaviewmodel.uiState.collectAsState()
    val currentStep = addAziendaState.currentStep
    val uid = userState.user.uid
    val isAgencyUpdateAddAzienda = addAziendaState.isAgencyAdded
    val isAgencyAddedUser = userState.hasLoadedAgency

    LaunchedEffect(isAgencyUpdateAddAzienda, isAgencyAddedUser) {
        if (isAgencyUpdateAddAzienda) {
            userviewmodel.onAddAziendaRole(addAziendaState.azienda.idAzienda)
        }
        if (isAgencyAddedUser && isAgencyUpdateAddAzienda) {
            onTerminate()
        }
    }

    if (addAziendaState.resultMsg != null || userState.resultMsg != null) {
        StatusDialog(
            message = "Caricamento completato con successo",
            DialogStatusType.SUCCESS,
            onDismiss = {
                addaziendaviewmodel.clearMessage()
                userviewmodel.clearMessage()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        StepHeader(currentStep = currentStep, totalSteps = 4, onLogout)

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                1 -> StepOne(addaziendaviewmodel)
                2 -> StepTwo(addaziendaviewmodel)
                3 -> StepThree(addaziendaviewmodel)
                4 -> StepFour(addaziendaviewmodel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NavigationButtons(
            currentStep = currentStep,
            totalSteps = 5,
            onPrevious = { addaziendaviewmodel.onCurrentStepDown() },
            onNext = { addaziendaviewmodel.onCurrentStepUp() },
            onComplete = { addaziendaviewmodel.aggiungiAzienda() },
            canProceed = addaziendaviewmodel.canProceedToNextStep(currentStep)
        )
    }
}

@Composable
 fun StepHeader(
    currentStep: Int,
    totalSteps: Int,
    onLogout: () -> Unit
) {
    Column {
        // Riga con titolo e pulsante logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Azienda",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Passo $currentStep di $totalSteps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pulsante logout elegante
            OutlinedButton(
                onClick ={ onLogout()},
                modifier = Modifier.padding(start = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Esci")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}
@Composable
private fun StepContainer(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@Composable
private fun StepOne(addaziendaviewmodel: AddAziendaViewModel) {
    val addAziendaState by addaziendaviewmodel.uiState.collectAsState()
    val nomeAzienda = addAziendaState.azienda.nome

    StepContainer(
        icon = Icons.Default.Business,
        title = "Nome dell'azienda",
        subtitle = "Iniziamo con le informazioni di base"
    ) {
        OutlinedTextField(
            value = nomeAzienda,
            onValueChange = { addaziendaviewmodel.onNomeAziendaChanged(it) },
            label = { Text("Nome azienda") },
            placeholder = { Text("Es: Azienda Innovativa Srl") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun StepTwo(addaziendaviewmodel: AddAziendaViewModel) {
    StepContainer(
        icon = Icons.Default.Group,
        title = "Numero di dipendenti",
        subtitle = "Seleziona il range che meglio descrive la tua azienda"
    ) {
        DipendentiSelector(addaziendaviewmodel)
    }
}

@Composable
private fun StepThree(addaziendaviewmodel: AddAziendaViewModel) {
    StepContainer(
        icon = Icons.Default.Settings,
        title = "Settore di attività",
        subtitle = "In che settore opera la tua azienda?"
    ) {
        SettoreSelector(addaziendaviewmodel)
    }
}

@Composable
private fun StepFour(addaziendaviewmodel: AddAziendaViewModel) {
    val addAziendaState by addaziendaviewmodel.uiState.collectAsState()
    val context = LocalContext.current

    StepContainer(
        icon = Icons.Default.LocationOn,
        title = "Indirizzo dell'azienda",
        subtitle = "Inserisci l'indirizzo completo per la localizzazione"
    ) {
        AddressInputSection(
            currentAddress = addAziendaState.indirizzoInput,
            addressCandidates = addAziendaState.indirizziCandidati,
            selectedAddress = addAziendaState.indirizzoSelezionato,
            isGeocoding = addAziendaState.isGeocoding,
            geocodingError = addAziendaState.geocodingError,
            onAddressChange = { addaziendaviewmodel.onIndirizzoChanged(it) },
            onSearchAddress = { addaziendaviewmodel.searchAddress(context) },
            onAddressSelect = { addaziendaviewmodel.onIndirizzoSelezionato(it) }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun AddressInputSection(
    currentAddress: String,
    addressCandidates: List<Address>,
    selectedAddress: Address?,
    isGeocoding: Boolean,
    geocodingError: String?,
    onAddressChange: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onAddressSelect: (Address) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = currentAddress,
                onValueChange = onAddressChange,
                label = { Text("Indirizzo completo") },
                placeholder = {
                    Text("Es: Via Roma 123, 89100 Reggio Calabria RC, Italia")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                isError = geocodingError != null
            )

            Button(
                onClick = onSearchAddress,
                enabled = currentAddress.isNotBlank() && !isGeocoding,
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                }
            }
        }

        // Suggerimento formato
        Text(
            text = "💡 Inserisci: Via/Piazza, numero civico, CAP, città, provincia, Italia",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Errore geocoding
        geocodingError?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (addressCandidates.isNotEmpty()) {
            Text(
                text = if (addressCandidates.size > 1) {
                    "Trovati ${addressCandidates.size} indirizzi. Seleziona quello corretto:"
                } else {
                    "Indirizzo trovato:"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addressCandidates) { address ->
                    AddressCandidateCard(
                        address = address,
                        isSelected = address == selectedAddress,
                        onSelect = { onAddressSelect(address) }
                    )
                }
            }
        }

        // Indirizzo selezionato con coordinate
        selectedAddress?.let { address ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Indirizzo confermato",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = address.getAddressLine(0) ?: "Indirizzo non disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "📍 Coordinate: ${String.format("%.6f", address.latitude)}, ${String.format("%.6f", address.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressCandidateCard(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = address.getAddressLine(0) ?: "Indirizzo sconosciuto",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                val details = buildList {
                    address.locality?.let { add("Città: $it") }
                    address.postalCode?.let { add("CAP: $it") }
                    address.countryName?.let { add("Paese: $it") }
                }.joinToString(" • ")

                if (details.isNotEmpty()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selezionato",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onPrevious,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Indietro")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (currentStep < totalSteps) {
            Button(
                onClick = onNext,
                enabled = canProceed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text("Avanti")
            }
        } else {
            Button(
                onClick = onComplete,
                enabled = canProceed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text("Completa configurazione")
            }
        }
    }
}