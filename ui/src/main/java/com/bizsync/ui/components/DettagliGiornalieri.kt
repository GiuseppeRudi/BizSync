package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.DettagliGiornalieri
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import java.time.format.DateTimeFormatter


@Composable
fun DettagliGiornalieri(
    dettagli: DettagliGiornalieri,
    turni: List<Turno>,
    colleghi : List<User>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Dettagli ${dettagli.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Informazioni dipartimento
                    if (dettagli.nomeDipartimento != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dipartimento: ${dettagli.nomeDipartimento}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )

                        // Orari dipartimento
                        if (dettagli.orarioAperturaDipartimento != null && dettagli.orarioChiusuraDipartimento != null) {
                            Text(
                                text = "Orari dipartimento: ${dettagli.orarioAperturaDipartimento!!.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${dettagli.orarioChiusuraDipartimento!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ore assegnate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${dettagli.oreTotaliAssegnate}h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = "Ore effettive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${dettagli.oreEffettive.toInt()}h ${((dettagli.oreEffettive % 1) * 60).toInt()}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (dettagli.orarioInizio != null && dettagli.orarioFine != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Orario turni: ${dettagli.orarioInizio!!.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${dettagli.orarioFine!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        items(turni) { turno ->

            TurnoAssegnatoCard(
                turno = turno, onEdit = {}, onDelete = {}, isIdentical = false,
                dipendenti = colleghi,
                modifier = Modifier
            )
        }

        if (dettagli.colleghi.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Colleghi in turno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        dettagli.colleghi.forEach { collega ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${collega.nome} ${collega.cognome}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}