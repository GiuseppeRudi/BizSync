package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SecurityUpdate
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.ui.theme.BizSyncColors
import com.bizsync.ui.theme.BizSyncDimensions

@Composable
fun SettingsManagementScreen(onBackClick: () -> Unit) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("Italiano") }
    var selectedTheme by remember { mutableStateOf("Sistema") }
    var dataUsageLimit by remember { mutableStateOf(500f) }
    var cacheSize by remember { mutableStateOf("2.4 GB") }
    var lastSync by remember { mutableStateOf("5 minuti fa") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizSyncColors.Background)
            .padding(BizSyncDimensions.SpacingMedium)
    ) {
        // Header
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
                text = "Impostazioni Sistema",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BizSyncColors.OnBackground
                ),
                modifier = Modifier.padding(start = BizSyncDimensions.SpacingSmall)
            )
        }

        Spacer(modifier = Modifier.height(BizSyncDimensions.SpacingMedium))

        // Contenuto scorrevole
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(BizSyncDimensions.SpacingMedium)
        ) {
            // Sezione Aspetto
            item {
                SettingsSection(
                    title = "Aspetto",
                    icon = Icons.Default.Palette
                ) {
                    // Tema
                    SettingsDropdownItem(
                        title = "Tema",
                        subtitle = "Seleziona l'aspetto dell'app",
                        selectedValue = selectedTheme,
                        options = listOf("Sistema", "Chiaro", "Scuro"),
                        onValueChange = { selectedTheme = it }
                    )

                    // Modalità scura (se tema non è "Sistema")
                    if (selectedTheme != "Sistema") {
                        SettingsSwitchItem(
                            title = "Modalità Scura",
                            subtitle = "Attiva il tema scuro",
                            icon = Icons.Default.DarkMode,
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }
                }
            }

            // Sezione Notifiche
            item {
                SettingsSection(
                    title = "Notifiche",
                    icon = Icons.Default.Notifications
                ) {
                    SettingsSwitchItem(
                        title = "Notifiche Push",
                        subtitle = "Ricevi notifiche per eventi importanti",
                        icon = Icons.Default.NotificationsActive,
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )

                    SettingsClickableItem(
                        title = "Gestione Notifiche",
                        subtitle = "Personalizza le notifiche",
                        icon = Icons.Default.Tune,
                        onClick = { /* Apri schermata notifiche */ }
                    )
                }
            }

            // Sezione Sincronizzazione
            item {
                SettingsSection(
                    title = "Sincronizzazione",
                    icon = Icons.Default.Sync
                ) {
                    SettingsSwitchItem(
                        title = "Sincronizzazione Automatica",
                        subtitle = "Sincronizza automaticamente i dati",
                        icon = Icons.Default.CloudSync,
                        checked = autoSyncEnabled,
                        onCheckedChange = { autoSyncEnabled = it }
                    )

                    SettingsInfoItem(
                        title = "Ultima Sincronizzazione",
                        subtitle = lastSync,
                        icon = Icons.Default.Schedule
                    )

                    SettingsClickableItem(
                        title = "Sincronizza Ora",
                        subtitle = "Forza la sincronizzazione",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            // Logica di sincronizzazione
                            lastSync = "Ora"
                        }
                    )
                }
            }

            // Sezione Lingua e Regione
            item {
                SettingsSection(
                    title = "Lingua e Regione",
                    icon = Icons.Default.Language
                ) {
                    SettingsDropdownItem(
                        title = "Lingua",
                        subtitle = "Seleziona la lingua dell'app",
                        selectedValue = selectedLanguage,
                        options = listOf("Italiano", "English", "Español", "Français"),
                        onValueChange = { selectedLanguage = it }
                    )
                }
            }

            // Sezione Dati e Storage
            item {
                SettingsSection(
                    title = "Dati e Storage",
                    icon = Icons.Default.Storage
                ) {
                    SettingsSliderItem(
                        title = "Limite Dati Mobili",
                        subtitle = "${dataUsageLimit.toInt()} MB/mese",
                        icon = Icons.Default.DataUsage,
                        value = dataUsageLimit,
                        valueRange = 100f..2000f,
                        onValueChange = { dataUsageLimit = it }
                    )

                    SettingsInfoItem(
                        title = "Cache Applicazione",
                        subtitle = cacheSize,
                        icon = Icons.Default.FolderOpen
                    )

                    SettingsClickableItem(
                        title = "Pulisci Cache",
                        subtitle = "Libera spazio rimuovendo file temporanei",
                        icon = Icons.Default.CleaningServices,
                        onClick = {
                            // Logica pulizia cache
                            cacheSize = "0 MB"
                        }
                    )
                }
            }

            // Sezione Sicurezza
            item {
                SettingsSection(
                    title = "Sicurezza",
                    icon = Icons.Default.Security
                ) {
                    SettingsClickableItem(
                        title = "Gestione Password",
                        subtitle = "Cambia password account",
                        icon = Icons.Default.Key,
                        onClick = { /* Apri gestione password */ }
                    )

                    SettingsClickableItem(
                        title = "Autenticazione a Due Fattori",
                        subtitle = "Migliora la sicurezza del tuo account",
                        icon = Icons.Default.SecurityUpdate,
                        onClick = { /* Apri 2FA */ }
                    )
                }
            }

            // Sezione Supporto
            item {
                SettingsSection(
                    title = "Supporto",
                    icon = Icons.Default.Help
                ) {
                    SettingsClickableItem(
                        title = "Centro Assistenza",
                        subtitle = "FAQ e guide utente",
                        icon = Icons.Default.HelpCenter,
                        onClick = { /* Apri centro assistenza */ }
                    )

                    SettingsClickableItem(
                        title = "Contatta Supporto",
                        subtitle = "Invia feedback o richiesta supporto",
                        icon = Icons.Default.ContactSupport,
                        onClick = { /* Apri supporto */ }
                    )

                    SettingsInfoItem(
                        title = "Versione App",
                        subtitle = "1.2.3 (Build 456)",
                        icon = Icons.Default.Info
                    )
                }
            }
        }
    }
}

// Composable per le sezioni
@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BizSyncColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(BizSyncDimensions.SpacingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = BizSyncDimensions.SpacingMedium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BizSyncColors.Primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingSmall))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BizSyncColors.OnSurface
                    )
                )
            }

            content()
        }
    }
}

// Switch Item
@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BizSyncDimensions.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BizSyncColors.OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = BizSyncColors.OnSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BizSyncColors.OnSurfaceVariant
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BizSyncColors.Primary,
                checkedTrackColor = BizSyncColors.Primary.copy(alpha = 0.5f)
            )
        )
    }
}

// Dropdown Item
@Composable
fun SettingsDropdownItem(
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BizSyncDimensions.SpacingSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = BizSyncColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BizSyncColors.OnSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BizSyncColors.OnSurfaceVariant
                    )
                )
            }

            Text(
                text = selectedValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BizSyncColors.Primary
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Clickable Item
@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = BizSyncDimensions.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BizSyncColors.OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = BizSyncColors.OnSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BizSyncColors.OnSurfaceVariant
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = BizSyncColors.OnSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Info Item (non cliccabile)
@Composable
fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BizSyncDimensions.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BizSyncColors.OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = BizSyncColors.OnSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BizSyncColors.OnSurfaceVariant
                )
            )
        }
    }
}

// Slider Item
@Composable
fun SettingsSliderItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BizSyncDimensions.SpacingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BizSyncColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(BizSyncDimensions.SpacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BizSyncColors.OnSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BizSyncColors.OnSurfaceVariant
                    )
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(horizontal = 32.dp),
            colors = SliderDefaults.colors(
                thumbColor = BizSyncColors.Primary,
                activeTrackColor = BizSyncColors.Primary
            )
        )
    }
}
