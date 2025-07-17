package com.bizsync.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.ui.theme.BizSyncColors
import com.bizsync.ui.theme.BizSyncDimensions

@Composable
fun ManagementTemplate(
    title: String,
    onBackClick: () -> Unit,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {
        // Header con titolo e back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = BizSyncColors.OnBackground
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BizSyncColors.OnBackground
                ),
                modifier = Modifier.padding(start = BizSyncDimensions.SpacingSmall)
            )
        }

        // Contenuto principale con stato "Coming Soon"
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icona principale
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = BizSyncColors.Primary.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingLarge))

                // Titolo principale
                Text(
                    text = "Funzione in Sviluppo",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BizSyncColors.OnBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingMedium))

                // Descrizione
                Text(
                    text = "Questa funzionalità sarà disponibile presto.\nStiamo lavorando per portarti la migliore esperienza possibile.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BizSyncColors.OnSurfaceVariant
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = BizSyncDimensions.SpacingLarge)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BizSyncDimensions.SpacingMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = BizSyncColors.Surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(BizSyncDimensions.SpacingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = BizSyncColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingSmall))

                            Text(
                                text = "Prossimo Aggiornamento",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = BizSyncColors.OnSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingSmall))

                        Text(
                            text = "Le nuove funzionalità di gestione saranno integrate nella prossima versione dell'app.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BizSyncColors.OnSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingLarge))

                // Indicatore di progresso animato
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = BizSyncColors.Primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = BizSyncColors.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}