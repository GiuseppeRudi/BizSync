package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val userVM = LocalUserViewModel.current

    val userState by userVM.uiState.collectAsState()
    val giornoPubblicazione = userState.azienda.giornoPubblicazioneTurni

    val giorniMancanti by remember(giornoPubblicazione) {
        mutableStateOf(calcolaGiorniAlProssimoGiorno(giornoPubblicazione))
    }

    // Messaggio
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFFe0f2f1)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hai $giorniMancanti giorni per pubblicare i turni della prossima settimana.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF00796b),
            modifier = Modifier.padding(16.dp)
        )
    }

    Button(onClick = { navController.navigate("pianifica") }) {
        Text("Vai a pianifica")
    }
}


fun calcolaGiorniAlProssimoGiorno(giornoTarget: DayOfWeek): Long {
    val oggi = LocalDate.now()
    val prossimoTarget = oggi.with(TemporalAdjusters.next(giornoTarget))
    return ChronoUnit.DAYS.between(oggi, prossimoTarget)
}
