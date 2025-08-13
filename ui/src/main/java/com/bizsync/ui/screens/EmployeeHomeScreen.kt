package com.bizsync.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.model.ProssimoTurno
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.domain.constants.enumClass.StatoTurno
import com.bizsync.domain.constants.enumClass.UrgencyLevel
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoWithDetails
import com.bizsync.domain.model.User
import com.bizsync.ui.components.BadgePreviewCard
import com.bizsync.ui.components.DialogsSection
import com.bizsync.ui.components.EmployeeShiftPublicationAlert
import com.bizsync.ui.components.EmployeeWelcomeHeader
import com.bizsync.ui.components.ProssimoTurnoCard
import com.bizsync.ui.components.TimbratureOggiCard
import com.bizsync.ui.components.TodayTurnoOverviewCard
import com.bizsync.ui.components.getCurrentLocation
import com.bizsync.ui.components.isLocationEnabled
import com.bizsync.ui.components.openLocationSettings
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.EmployeeHomeState
import com.bizsync.ui.model.UserState
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.EmployeeHomeViewModel
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    modifier: Modifier = Modifier,
    userState: UserState,
    viewModel: EmployeeHomeViewModel = hiltViewModel(),
    onNavigate: (HomeScreenRoute) -> Unit
) {
    val homeState by viewModel.homeState.collectAsState()
    val currentTime = remember { mutableStateOf(LocalDateTime.now()) }

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showLocationDialog by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }
    var showLocationErrorDialog by remember { mutableStateOf(false) }
    var locationErrorMessage by remember { mutableStateOf("") }
    var pendingTimbratura by remember { mutableStateOf<TipoTimbratura?>(null) }

    val context = LocalContext.current

    // Inizializza dati dipendente
    LaunchedEffect(userState) {
        viewModel.initializeEmployee(userState)
    }

    // Aggiorna l'orario ogni minuto
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime.value = LocalDateTime.now()
        }
    }

    // Gestione automatica richieste posizione
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && pendingTimbratura != null) {
            if (isLocationEnabled(context)) {
                val tipoTimbraturaTemp = pendingTimbratura!!
                pendingTimbratura = null

                viewModel.setIsGettingLocation(true)
                getCurrentLocation(
                    context = context,
                    onLocationReceived = { lat, lon ->
                        viewModel.setIsGettingLocation(false)
                        viewModel.onTimbra(tipoTimbraturaTemp, lat, lon)
                    },
                    onError = { error ->
                        viewModel.setIsGettingLocation(false)
                        locationErrorMessage = error
                        showLocationErrorDialog = true
                    }
                )
            } else {
                showGpsDialog = true
            }
        }
    }

    // Dialog gestione errori e permessi
    DialogsSection(
        showLocationDialog = showLocationDialog,
        showGpsDialog = showGpsDialog,
        showLocationErrorDialog = showLocationErrorDialog,
        locationErrorMessage = locationErrorMessage,
        homeState = homeState,
        pendingTimbratura = pendingTimbratura,
        onLocationDialogConfirm = {
            locationPermissionState.launchMultiplePermissionRequest()
            showLocationDialog = false
        },
        onLocationDialogDismiss = {
            showLocationDialog = false
            pendingTimbratura = null
        },
        onGpsDialogOpenSettings = {
            openLocationSettings(context)
            showGpsDialog = false
        },
        onGpsDialogDismiss = {
            showGpsDialog = false
            pendingTimbratura = null
        },
        onLocationErrorRetry = {
            showLocationErrorDialog = false
            pendingTimbratura?.let { tipo ->
                viewModel.setIsGettingLocation(true)
                getCurrentLocation(
                    context = context,
                    onLocationReceived = { lat, lon ->
                        viewModel.setIsGettingLocation(false)
                        viewModel.onTimbra(tipo, lat, lon)
                    },
                    onError = { error ->
                        viewModel.setIsGettingLocation(false)
                        locationErrorMessage = error
                        showLocationErrorDialog = true
                    }
                )
            }
        },
        onLocationErrorDismiss = {
            showLocationErrorDialog = false
            pendingTimbratura = null
        },
        onErrorDismiss = viewModel::dismissError,
        onSuccessDismiss = viewModel::dismissSuccess
    )

    // Layout principale
    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header con benvenuto
            item {
                EmployeeWelcomeHeader(
                    user = userState.user.toDomain(),
                    azienda = userState.azienda.toDomain(),
                    currentTime = currentTime.value
                )
            }


            Log.d("DENTRO", "SONO DENTRO")

            homeState.daysUntilShiftPublication?.let {
                item {
                    EmployeeShiftPublicationAlert(
                        daysUntilPublication = homeState.daysUntilShiftPublication!!
                    )
                }
            }

            // Badge preview
            item {
                BadgePreviewCard(
                    user = userState.user,
                    onClick = { onNavigate(HomeScreenRoute.Badge) },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Prossimo turno con timer
            homeState.prossimoTurno?.let { prossimoTurno ->
                item {
                    ProssimoTurnoCard(
                        prossimoTurno = prossimoTurno,
                        canTimbra = homeState.canTimbra,
                        isGettingLocation = homeState.isGettingLocation,
                        onTimbra = { tipo, _ ->
                            pendingTimbratura = tipo

                            val turno = prossimoTurno.turno
                            val zonaLavorativa = turno?.getZonaLavorativaDipendente(userState.user.uid)

                            if (zonaLavorativa == ZonaLavorativa.IN_SEDE) {
                                when {
                                    !locationPermissionState.allPermissionsGranted -> {
                                        showLocationDialog = true
                                    }
                                    !isLocationEnabled(context) -> {
                                        showGpsDialog = true
                                    }
                                    else -> {
                                        viewModel.setIsGettingLocation(true)
                                        getCurrentLocation(
                                            context = context,
                                            onLocationReceived = { lat, lon ->
                                                viewModel.setIsGettingLocation(false)
                                                viewModel.onTimbra(tipo, lat, lon)
                                            },
                                            onError = { error ->
                                                viewModel.setIsGettingLocation(false)
                                                locationErrorMessage = error
                                                showLocationErrorDialog = true
                                            }
                                        )
                                    }
                                }
                            } else {
                                viewModel.onTimbra(tipo)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            homeState.todayTurno?.let { turnoDetails ->
                item {
                    TodayTurnoOverviewCard(
                        turnoWithDetails = turnoDetails,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (homeState.timbratureOggi.isNotEmpty()) {
                item {
                    TimbratureOggiCard(
                        timbrature = homeState.timbratureOggi,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }

        // Loading overlay
        if (homeState.isLoading || homeState.isGettingLocation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    if (homeState.isGettingLocation) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Rilevamento posizione...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


