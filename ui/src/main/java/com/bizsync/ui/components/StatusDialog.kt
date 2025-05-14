package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class DialogStatusType {
    SUCCESS,
    ERROR
}

data class DialogStatusInfo(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
fun StatusDialog(
    message: String?,
    statusType: DialogStatusType,
    onDismiss: () -> Unit
) {
    val statusInfo = when (statusType) {
        DialogStatusType.SUCCESS -> DialogStatusInfo(
            title = "Successo",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF4CAF50),
            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
        DialogStatusType.ERROR -> DialogStatusInfo(
            title = "Errore",
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFF44336),
            backgroundColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    }

    if(message!=null)
    {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(statusInfo.backgroundColor, shape = MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = null,
                            tint = statusInfo.iconColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = statusInfo.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = statusInfo.iconColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onDismiss) {
                        Text("Chiudi")
                    }
                }
            }
        }

    }

}
