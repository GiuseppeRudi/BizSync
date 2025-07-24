package com.bizsync.app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.model.BadgeVirtuale
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
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.User
import com.bizsync.ui.components.TimbratureOggiCard
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.UserState
import com.bizsync.ui.viewmodels.EmployeeHomeState
import com.bizsync.ui.viewmodels.EmployeeHomeViewModel
import com.bizsync.ui.viewmodels.StatoTurno
import com.bizsync.ui.viewmodels.TurnoWithDetails
import com.bizsync.ui.viewmodels.UrgencyLevel
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

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    badge = homeState.badge,
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

            // Panoramica turno di oggi
            homeState.todayTurno?.let { turnoDetails ->
                item {
                    TodayTurnoOverviewCard(
                        turnoWithDetails = turnoDetails,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Timbrature di oggi
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

@Composable
fun EmployeeWelcomeHeader(
    user: User,
    azienda: Azienda,
    currentTime: LocalDateTime
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ITALIAN)

    val greeting = when (currentTime.hour) {
        in 5..11 -> "Buongiorno"
        in 12..17 -> "Buon pomeriggio"
        else -> "Buonasera"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting, ${user.nome}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = azienda.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dipendente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeShiftPublicationAlert(
    daysUntilPublication: Int
) {
    val urgencyLevel = when {
        daysUntilPublication <= 0 -> UrgencyLevel.CRITICAL
        daysUntilPublication == 1 -> UrgencyLevel.HIGH
        daysUntilPublication <= 2 -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }

    val colors = when (urgencyLevel) {
        UrgencyLevel.CRITICAL -> Triple(
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            Icons.Default.Schedule
        )
        UrgencyLevel.HIGH -> Triple(
            Color(0xFFFF9800),
            Color(0xFFFFF3E0),
            Icons.Default.Schedule
        )
        UrgencyLevel.MEDIUM -> Triple(
            Color(0xFFFFC107),
            Color(0xFFFFFDE7),
             Icons.Default.Schedule
        )
        UrgencyLevel.LOW -> Triple(
            Color(0xFF2196F3),
            Color(0xFFE3F2FD),
            Icons.Default.Schedule
        )
    }

    val message = when {
        daysUntilPublication < 0 -> "I turni della prossima settimana sono in ritardo!"
        daysUntilPublication == 0 -> "I turni vengono pubblicati oggi!"
        daysUntilPublication == 1 -> "I turni vengono pubblicati domani"
        else -> "I turni saranno pubblicati tra $daysUntilPublication giorni"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.second
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = colors.third,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.first
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📅 Pubblicazione Turni",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.first
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TodayTurnoOverviewCard(
    turnoWithDetails: TurnoWithDetails,
    modifier: Modifier = Modifier
) {
    val statoColor = when (turnoWithDetails.statoTurno) {
        StatoTurno.NON_INIZIATO -> Color(0xFF9E9E9E)
        StatoTurno.IN_CORSO -> Color(0xFF4CAF50)
        StatoTurno.IN_PAUSA -> Color(0xFFFF9800)
        StatoTurno.COMPLETATO -> Color(0xFF2196F3)
        StatoTurno.IN_RITARDO -> Color(0xFFFF5722)
        StatoTurno.ASSENTE -> Color(0xFFF44336)
    }

    val statoText = when (turnoWithDetails.statoTurno) {
        StatoTurno.NON_INIZIATO -> "Non iniziato"
        StatoTurno.IN_CORSO -> "In corso"
        StatoTurno.IN_PAUSA -> "In pausa"
        StatoTurno.COMPLETATO -> "Completato"
        StatoTurno.IN_RITARDO -> "In ritardo"
        StatoTurno.ASSENTE -> "Assente"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turno di Oggi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = turnoWithDetails.turno.titolo,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Badge(
                    containerColor = statoColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statoText,
                        color = statoColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orari
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Orario Previsto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${turnoWithDetails.turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turnoWithDetails.turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (turnoWithDetails.orarioEntrataEffettivo != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Entrata Effettiva",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = turnoWithDetails.orarioEntrataEffettivo!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (turnoWithDetails.minutiRitardo > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Indicatori ritardo/anticipo
            if (turnoWithDetails.minutiRitardo > 0 || turnoWithDetails.minutiAnticipo > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                if (turnoWithDetails.minutiRitardo > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF44336).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⏰ Ritardo: ${turnoWithDetails.minutiRitardo} minuti",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (turnoWithDetails.minutiAnticipo > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⚡ Uscita anticipata: ${turnoWithDetails.minutiAnticipo} minuti",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar dello stato del turno
            val progress = when {
                turnoWithDetails.statoTurno == StatoTurno.COMPLETATO -> 1f
                turnoWithDetails.haTimbratoEntrata -> 0.5f
                else -> 0f
            }

            LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
            color = statoColor,
            trackColor = statoColor.copy(alpha = 0.2f),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}


@Composable
fun DialogsSection(
    showLocationDialog: Boolean,
    showGpsDialog: Boolean,
    showLocationErrorDialog: Boolean,
    locationErrorMessage: String,
    homeState: EmployeeHomeState,
    pendingTimbratura: TipoTimbratura?,
    onLocationDialogConfirm: () -> Unit,
    onLocationDialogDismiss: () -> Unit,
    onGpsDialogOpenSettings: () -> Unit,
    onGpsDialogDismiss: () -> Unit,
    onLocationErrorRetry: () -> Unit,
    onLocationErrorDismiss: () -> Unit,
    onErrorDismiss: () -> Unit,
    onSuccessDismiss: () -> Unit
) {
    // Dialog permessi posizione
    if (showLocationDialog) {
        LocationPermissionDialog(
            onConfirm = onLocationDialogConfirm,
            onDismiss = onLocationDialogDismiss
        )
    }

    // Dialog GPS disattivato
    if (showGpsDialog) {
        GpsDisabledDialog(
            onOpenSettings = onGpsDialogOpenSettings,
            onDismiss = onGpsDialogDismiss
        )
    }

    // Dialog errore posizione
    if (showLocationErrorDialog) {
        LocationErrorDialog(
            message = locationErrorMessage,
            onRetry = onLocationErrorRetry,
            onDismiss = onLocationErrorDismiss
        )
    }

    // Dialog errore generale
    homeState.error?.let { error ->
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            title = { Text("Errore") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog successo
    if (homeState.showSuccess) {
        SuccessDialog(
            message = homeState.successMessage,
            onDismiss = onSuccessDismiss
        )
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    onLocationReceived: (Double, Double) -> Unit,
    onError: (String) -> Unit = {}
) {
    Log.d("LOCATION_DEBUG", "=== INIZIO getCurrentLocation ===")

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Prima prova con lastLocation (veloce)
    Log.d("LOCATION_DEBUG", "Tentativo 1: Recupero lastLocation dalla cache...")
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        Log.d("LOCATION_DEBUG", "LastLocation callback success")
        if (location != null) {
            Log.d("LOCATION_DEBUG", "✅ LastLocation trovata: ${location.latitude}, ${location.longitude}")
            Log.d("LOCATION_DEBUG", "Accuratezza: ${location.accuracy}m, Età: ${System.currentTimeMillis() - location.time}ms")
            onLocationReceived(location.latitude, location.longitude)
        } else {
            Log.w("LOCATION_DEBUG", "⚠️ LastLocation è null, richiedo posizione aggiornata")
            requestCurrentLocation(context, onLocationReceived, onError)
        }
    }.addOnFailureListener { exception ->
        Log.e("LOCATION_DEBUG", "❌ Errore nel recuperare lastLocation: ${exception.message}")
        requestCurrentLocation(context, onLocationReceived, onError)
    }
}

@SuppressLint("MissingPermission")
private fun requestCurrentLocation(
    context: Context,
    onLocationReceived: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    Log.d("LOCATION_DEBUG", "=== INIZIO requestCurrentLocation ===")

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .setMaxUpdateDelayMillis(15000)
        .build()

    Log.d("LOCATION_DEBUG", "LocationRequest configurato: HIGH_ACCURACY, 10s interval")

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("LOCATION_DEBUG", "LocationCallback.onLocationResult chiamato")

            val location = locationResult.lastLocation
            if (location != null) {
                Log.d("LOCATION_DEBUG", "✅ Nuova posizione ottenuta:")
                Log.d("LOCATION_DEBUG", "  Latitudine: ${location.latitude}")
                Log.d("LOCATION_DEBUG", "  Longitudine: ${location.longitude}")
                Log.d("LOCATION_DEBUG", "  Accuratezza: ${location.accuracy}m")
                Log.d("LOCATION_DEBUG", "  Provider: ${location.provider}")
                Log.d("LOCATION_DEBUG", "  Età: ${System.currentTimeMillis() - location.time}ms")

                onLocationReceived(location.latitude, location.longitude)

                // Interrompi le richieste di posizione
                fusedLocationClient.removeLocationUpdates(this)
                Log.d("LOCATION_DEBUG", "Location updates rimossi")
            } else {
                Log.e("LOCATION_DEBUG", "❌ LocationResult.lastLocation è null")
                onError("Impossibile ottenere la posizione")
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            Log.d("LOCATION_DEBUG", "LocationAvailability: ${availability.isLocationAvailable}")
            if (!availability.isLocationAvailable) {
                Log.e("LOCATION_DEBUG", "❌ Servizi di localizzazione non disponibili")
                onError("GPS non disponibile. Attiva la localizzazione nelle impostazioni.")
            }
        }
    }

    // Timeout dopo 15 secondi
    val handler = android.os.Handler(android.os.Looper.getMainLooper())
    val timeoutRunnable = Runnable {
        Log.e("LOCATION_DEBUG", "❌ TIMEOUT: 15 secondi scaduti")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        onError("Timeout: impossibile ottenere la posizione entro 15 secondi")
    }
    handler.postDelayed(timeoutRunnable, 15000)

    try {
        Log.d("LOCATION_DEBUG", "Invio richiesta location updates...")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            android.os.Looper.getMainLooper()
        )
        Log.d("LOCATION_DEBUG", "✅ Richiesta di posizione inviata con successo")
    } catch (e: Exception) {
        Log.e("LOCATION_DEBUG", "❌ Eccezione nella richiesta di posizione: ${e.message}")
        handler.removeCallbacks(timeoutRunnable)
        onError("Errore nel richiedere la posizione: ${e.message}")
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    Log.d("LOCATION_DEBUG", "GPS Provider enabled: $gpsEnabled")
    Log.d("LOCATION_DEBUG", "Network Provider enabled: $networkEnabled")

    return gpsEnabled || networkEnabled
}

@Composable
fun GpsDisabledDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.GpsOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
        },
        title = {
            Text("GPS disattivato")
        },
        text = {
            Text("Per timbrare in sede è necessario attivare i servizi di localizzazione. Apri le impostazioni e attiva il GPS.")
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("APRI IMPOSTAZIONI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ANNULLA")
            }
        }
    )
}

@Composable
fun LocationErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationSearching,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Yellow
            )
        },
        title = {
            Text("Errore di localizzazione")
        },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("RIPROVA")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ANNULLA")
            }
        }
    )
}

@Composable
fun BadgePreviewCard(
    badge: BadgeVirtuale?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto profilo
            AsyncImage(
                model = badge?.fotoUrl ?: "",
                contentDescription = "Foto profilo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info dipendente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge?.getFullName() ?: "Nome Dipendente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = badge?.posizioneLavorativa ?: "Posizione",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Matricola: ${badge?.matricola ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = "Badge",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProssimoTurnoCard(
    modifier: Modifier = Modifier,
    prossimoTurno: ProssimoTurno,
    canTimbra: Boolean,
    isGettingLocation: Boolean = false,
    onTimbra: (TipoTimbratura, Context) -> Unit,
) {
    var currentTime by remember { mutableStateOf(prossimoTurno.getTempoMancanteFormattato()) }

    val context = LocalContext.current

    LaunchedEffect(prossimoTurno) {
        while (true) {
            currentTime = prossimoTurno.getTempoMancanteFormattato()
            delay(1000)
        }
    }

    val cardColor = when {
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.ENTRATA ->
            MaterialTheme.colorScheme.primaryContainer
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.USCITA ->
            MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when {
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.ENTRATA ->
            MaterialTheme.colorScheme.primary
        canTimbra && prossimoTurno.tipoTimbraturaNecessaria == TipoTimbratura.USCITA ->
            MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon = when (prossimoTurno.tipoTimbraturaNecessaria) {
                TipoTimbratura.ENTRATA -> Icons.AutoMirrored.Filled.Login
                TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.Logout
            }

            Icon(
                imageVector = icon,
                contentDescription = "Timbratura",
                modifier = Modifier.size(48.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stato del turno
            Text(
                text = prossimoTurno.getStatoTurno(),
                style = MaterialTheme.typography.titleMedium,
                color = when (prossimoTurno.tipoTimbraturaNecessaria) {
                    TipoTimbratura.ENTRATA -> MaterialTheme.colorScheme.primary
                    TipoTimbratura.USCITA -> MaterialTheme.colorScheme.secondary
                }
            )

            prossimoTurno.turno?.let { turno ->
                Text(
                    text = turno.titolo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - " +
                            turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mostra lo stato delle timbrature con design migliorato
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicatore entrata
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (prossimoTurno.haTimbratoEntrata)
                                    Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Entrata",
                                modifier = Modifier.size(20.dp),
                                tint = if (prossimoTurno.haTimbratoEntrata)
                                    Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Entrata",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (prossimoTurno.haTimbratoEntrata)
                                    Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Linea di collegamento
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(40.dp)
                                .background(
                                    if (prossimoTurno.haTimbratoEntrata)
                                        Color.Green
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(1.dp)
                                )
                        )

                        // Indicatore uscita
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (prossimoTurno.haTimbratoUscita)
                                    Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Uscita",
                                modifier = Modifier.size(20.dp),
                                tint = if (prossimoTurno.haTimbratoUscita)
                                    Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Uscita",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (prossimoTurno.haTimbratoUscita)
                                    Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = currentTime,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { time ->
                        Text(
                            text = time,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    }

                    Text(
                        text = prossimoTurno.messaggioStato,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (canTimbra) {
                Spacer(modifier = Modifier.height(20.dp))

                // Pulsante dinamico
                val buttonColor = when (prossimoTurno.tipoTimbraturaNecessaria) {
                    TipoTimbratura.ENTRATA -> MaterialTheme.colorScheme.primary
                    TipoTimbratura.USCITA -> MaterialTheme.colorScheme.secondary
                }

                Button(
                    onClick = {
                        if (!isGettingLocation) {
                            onTimbra(prossimoTurno.tipoTimbraturaNecessaria, context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isGettingLocation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGettingLocation)
                            buttonColor.copy(alpha = 0.6f)
                        else buttonColor
                    )
                ) {
                    AnimatedContent(
                        targetState = isGettingLocation,
                        transitionSpec = {
                            fadeIn(tween(300)) with fadeOut(tween(300))
                        }
                    ) { loading ->
                        if (loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "RILEVAMENTO POSIZIONE...",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (prossimoTurno.tipoTimbraturaNecessaria) {
                                        TipoTimbratura.ENTRATA -> Icons.Default.Fingerprint
                                        TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.ExitToApp
                                    },
                                    contentDescription = "Timbra",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prossimoTurno.getTestoPulsante(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isGettingLocation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Assicurati che il GPS sia attivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = when {
                            prossimoTurno.haTimbratoEntrata && prossimoTurno.haTimbratoUscita ->
                                "✅ Turno completato"
                            (prossimoTurno.tempoMancante?.toMinutes() ?: 0) > 30 ->
                                "Timbratura non ancora disponibile"
                            else -> "Finestra di timbratura non attiva"
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Green
            )
        },
        title = {
            Text(
                text = "Successo",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Permesso posizione richiesto",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Per timbrare in sede è necessario verificare che ti trovi presso l'azienda. " +
                        "La tua posizione esatta non verrà salvata o mostrata al manager per privacy.",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("AUTORIZZA")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ANNULLA")
            }
        }
    )
}



fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}


