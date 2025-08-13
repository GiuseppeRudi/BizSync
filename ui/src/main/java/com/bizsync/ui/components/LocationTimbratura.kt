package com.bizsync.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.ui.model.EmployeeHomeState
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

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
    val handler = Handler(Looper.getMainLooper())
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
            Looper.getMainLooper()
        )
        Log.d("LOCATION_DEBUG", " Richiesta di posizione inviata con successo")
    } catch (e: Exception) {
        Log.e("LOCATION_DEBUG", " Eccezione nella richiesta di posizione: ${e.message}")
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


