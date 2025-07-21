package com.bizsync.app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.model.Timbratura
import com.bizsync.ui.viewmodels.HomeViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.ui.components.TimbratureOggiCard
import com.bizsync.ui.model.UserState
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

// Sostituisci la logica nella EmployeeHomeScreen con questa versione migliorata:

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmployeeHomeScreen(
    userState: UserState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()

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
    var isGettingLocation by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(userState) {
        viewModel.initializeEmployee(userState)
    }

    // Osserva i cambiamenti dei permessi per riprovare automaticamente
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && pendingTimbratura != null) {
            // Controlla se GPS è attivo
            if (isLocationEnabled(context)) {
                // Riprova automaticamente a ottenere la posizione
                val tipoTimbraturaTemp = pendingTimbratura!!
                pendingTimbratura = null

                isGettingLocation = true
                getCurrentLocation(
                    context = context,
                    onLocationReceived = { lat, lon ->
                        isGettingLocation = false
                        viewModel.onTimbra(tipoTimbraturaTemp, lat, lon)
                    },
                    onError = { error ->
                        isGettingLocation = false
                        locationErrorMessage = error
                        showLocationErrorDialog = true
                    }
                )
            } else {
                showGpsDialog = true
            }
        }
    }

    // Dialog permessi posizione
    if (showLocationDialog) {
        LocationPermissionDialog(
            onConfirm = {
                locationPermissionState.launchMultiplePermissionRequest()
                showLocationDialog = false
            },
            onDismiss = {
                showLocationDialog = false
                pendingTimbratura = null
            }
        )
    }

    // Dialog GPS disattivato
    if (showGpsDialog) {
        GpsDisabledDialog(
            onOpenSettings = {
                openLocationSettings(context)
                showGpsDialog = false
            },
            onDismiss = {
                showGpsDialog = false
                pendingTimbratura = null
            }
        )
    }

    // Dialog errore posizione
    if (showLocationErrorDialog) {
        LocationErrorDialog(
            message = locationErrorMessage,
            onRetry = {
                showLocationErrorDialog = false
                pendingTimbratura?.let { tipo ->
                    isGettingLocation = true
                    getCurrentLocation(
                        context = context,
                        onLocationReceived = { lat, lon ->
                            isGettingLocation = false
                            viewModel.onTimbra(tipo, lat, lon)
                        },
                        onError = { error ->
                            isGettingLocation = false
                            locationErrorMessage = error
                            showLocationErrorDialog = true
                        }
                    )
                }
            },
            onDismiss = {
                showLocationErrorDialog = false
                pendingTimbratura = null
            }
        )
    }

    // Dialog errore generale
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Errore") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog successo
    if (uiState.showSuccess) {
        SuccessDialog(
            message = uiState.successMessage,
            onDismiss = viewModel::dismissSuccess
        )
    }

    // Layout principale
    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con badge preview
            BadgePreviewCard(
                badge = uiState.badge,
                onClick = { viewModel.changeCurrentScreen(HomeScreenRoute.Badge) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card prossimo turno con timer
            // Nella parte del Card prossimo turno con timer, aggiorna questa sezione:
            timerState?.let { prossimo ->
                ProssimoTurnoCard(
                    prossimoTurno = prossimo,
                    canTimbra = uiState.canTimbra,
                    isGettingLocation = isGettingLocation,
                    onTimbra = { tipo, _ ->
                        Log.d("TIMBRATURA_DEBUG", "=== INIZIO PROCESSO TIMBRATURA ===")
                        Log.d("TIMBRATURA_DEBUG", "Tipo timbratura: $tipo")

                        pendingTimbratura = tipo

                        val turno = prossimo.turno
                        val zonaLavorativa = turno?.getZonaLavorativaDipendente(userState.user.uid)
                        Log.d("TIMBRATURA_DEBUG", "Zona lavorativa: $zonaLavorativa")

                        if (zonaLavorativa == ZonaLavorativa.IN_SEDE) {
                            Log.d("TIMBRATURA_DEBUG", "Dipendente in sede - richiesta posizione necessaria")

                            when {
                                !locationPermissionState.allPermissionsGranted -> {
                                    Log.d("TIMBRATURA_DEBUG", "Permessi mancanti - mostro dialog")
                                    showLocationDialog = true
                                }
                                !isLocationEnabled(context) -> {
                                    Log.d("TIMBRATURA_DEBUG", "GPS disattivato - mostro dialog")
                                    showGpsDialog = true
                                }
                                else -> {
                                    Log.d("TIMBRATURA_DEBUG", "Permessi OK e GPS attivo - ottengo posizione")
                                    isGettingLocation = true
                                    getCurrentLocation(
                                        context = context,
                                        onLocationReceived = { lat, lon ->
                                            Log.d("TIMBRATURA_DEBUG", "✅ Posizione ricevuta: lat=$lat, lon=$lon")
                                            isGettingLocation = false
                                            viewModel.onTimbra(tipo, lat, lon)
                                        },
                                        onError = { error ->
                                            Log.e("TIMBRATURA_DEBUG", "❌ Errore posizione: $error")
                                            isGettingLocation = false
                                            locationErrorMessage = error
                                            showLocationErrorDialog = true
                                        }
                                    )
                                }
                            }
                        } else {
                            Log.d("TIMBRATURA_DEBUG", "Dipendente non in sede - timbratura senza GPS")
                            viewModel.onTimbra(tipo)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timbrature di oggi
            if (uiState.timbratureOggi.isNotEmpty()) {
                TimbratureOggiCard(
                    timbrature = uiState.timbratureOggi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Loading overlay
        if (uiState.isLoading || isGettingLocation) {
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
                    if (isGettingLocation) {
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

// Aggiungi anche controlli di stato GPS e permessi:
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    Log.d("LOCATION_DEBUG", "GPS Provider enabled: $gpsEnabled")
    Log.d("LOCATION_DEBUG", "Network Provider enabled: $networkEnabled")

    return gpsEnabled || networkEnabled
}
// Nuovi dialog per gestire gli errori di posizione

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

// 2. Card preview del badge
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
// Aggiorna il componente ProssimoTurnoCard nella screen:
// Componente ProssimoTurnoCard completo aggiornato

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProssimoTurnoCard(
    prossimoTurno: ProssimoTurno,
    canTimbra: Boolean,
    isGettingLocation: Boolean = false,
    onTimbra: (TipoTimbratura, Context) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(prossimoTurno.getTempoMancanteFormattato()) }

    val context = LocalContext.current

    LaunchedEffect(prossimoTurno) {
        while (true) {
            currentTime = prossimoTurno.getTempoMancanteFormattato()
            delay(1000)
        }
    }

    // Colori dinamici basati sul tipo di timbratura
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
            // Icona dinamica basata sul tipo di timbratura
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

            // Timer countdown con effetti visivi
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

            // Pulsante di timbratura con stati diversi
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
                                        TipoTimbratura.USCITA -> Icons.Default.ExitToApp
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

                // Messaggio informativo sotto il pulsante
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
                // Mostra messaggio quando non può timbrare
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
                            prossimoTurno.tempoMancante?.toMinutes() ?: 0 > 30 ->
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

// 6. Success Dialog
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


// 5. Dialog permessi posizione
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



// E questa per aprire le impostazioni GPS
fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}


