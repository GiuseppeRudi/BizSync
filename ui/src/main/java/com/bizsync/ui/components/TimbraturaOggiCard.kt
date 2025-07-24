package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.model.Timbratura
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach

@Composable
fun TimbratureOggiCard(
    timbrature: List<Timbratura>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Timbrature di oggi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            timbrature.forEach { timbratura ->
                TimbraturaMiniCard(timbratura = timbratura)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TimbraturaMiniCard(timbratura: Timbratura) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (timbratura.tipoTimbratura) {
                    TipoTimbratura.ENTRATA -> Icons.AutoMirrored.Filled.Login
                    TipoTimbratura.USCITA -> Icons.AutoMirrored.Filled.Logout
                },
                contentDescription = null,
                tint = if (timbratura.isAnomala()) Color.Red else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = timbratura.tipoTimbratura.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timbratura.dataOraTimbratura.format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Indicatori stato
        Row {
            if (!timbratura.dentroDellaTolleranza) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = "Posizione non corretta",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (timbratura.statoTimbratura != StatoTimbratura.IN_ORARIO) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Fuori orario",
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
