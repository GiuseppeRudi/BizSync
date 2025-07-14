package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.AreaLavoro

@Composable
fun RiepilogoGiornataCard(dipartimenti: List<AreaLavoro>) {
    val totaleCompleti = dipartimenti.count { /* TODO: calcola se completo */ false }
    val totaleParziali = dipartimenti.count { /* TODO: calcola se parziale */ true }
    val totaleIncompleti = dipartimenti.size - totaleCompleti - totaleParziali

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Riepilogo Giornata",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusChip(
                    icon = Icons.Default.CheckCircle,
                    label = "Completi",
                    count = totaleCompleti,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(
                    icon = Icons.Default.Warning,
                    label = "Parziali",
                    count = totaleParziali,
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatusChip(
                    icon = Icons.Default.Circle,
                    label = "Da fare",
                    count = totaleIncompleti,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}