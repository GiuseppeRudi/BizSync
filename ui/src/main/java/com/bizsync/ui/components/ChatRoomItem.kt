package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.ChatType
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Chat
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun ChatRoomItem(
    chat: Chat,
    dipartimenti : List<AreaLavoro>,
    onClick: () -> Unit
) {


    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar/Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (chat.tipo) {
                            ChatType.GENERALE -> Color(0xFF3498DB)
                            ChatType.DIPARTIMENTO -> getDepartmentColor(chat.dipartimento , dipartimenti)
                            ChatType.PRIVATA -> Color(0xFF95A5A6)
                            else -> {Color(0xFF95A5A6)}
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (chat.tipo) {
                        ChatType.GENERALE -> Icons.Default.Public
                        ChatType.DIPARTIMENTO -> getDepartmentIcon(chat.dipartimento, dipartimenti )
                        ChatType.PRIVATA -> Icons.Default.Person
                        else -> {Icons.Default.Person}
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenuto principale
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(chat.tipo == ChatType.GENERALE)
                    {
                        Text(
                            text = "Chat Generale",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 16.sp
                        )
                    }

                    if(chat.tipo == ChatType.PRIVATA)
                    {
                        Text(
                            text = chat.nome,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 16.sp
                        )
                    }

                    chat.dipartimento?.let {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (chat.ultimoMessaggioTimestamp != null) {
                        Text(
                            text = timeFormat.format(chat.ultimoMessaggioTimestamp),
                            color = Color(0xFF95A5A6),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.ultimoMessaggio ?: "Nessun messaggio",
                        color = Color(0xFF7F8C8D),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.messaggiNonLetti > 0) {
                        Badge(
                            containerColor = Color(0xFFE74C3C)
                        ) {
                            Text(
                                text = chat.messaggiNonLetti.toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


val departmentIcons = listOf(
    Icons.Default.People,
    Icons.Default.Computer,
    Icons.Default.Campaign,
    Icons.Default.TrendingUp,
    Icons.Default.Engineering,
    Icons.Default.Build,
    Icons.Default.SupportAgent,
    Icons.Default.MonitorHeart,
    Icons.Default.Inventory,
    Icons.Default.Workspaces
)


fun getDepartmentIcon(department: String?, dipartimenti: List<AreaLavoro>): ImageVector {
    val index = dipartimenti.indexOfFirst { it.nomeArea == department }
    return if (index in departmentIcons.indices) {
        departmentIcons[index]
    } else {
        Icons.Default.Group // fallback, non dovrebbe mai servire se max è 10
    }
}


val departmentColors = listOf(
    Color(0xFFE57373), // rosso
    Color(0xFF64B5F6), // blu
    Color(0xFF81C784), // verde
    Color(0xFFFFD54F), // giallo
    Color(0xFFBA68C8), // viola
    Color(0xFFFF8A65), // arancio
    Color(0xFFA1887F), // marrone
    Color(0xFF4DB6AC), // turchese
    Color(0xFF90A4AE), // grigio
    Color(0xFFFFB74D)  // arancio chiaro
)

fun getDepartmentColor(department: String?, dipartimenti: List<AreaLavoro>): Color {
    val index = dipartimenti.indexOfFirst { it.nomeArea == department }
    return if (index in departmentColors.indices) {
        departmentColors[index]
    } else {
        Color.Gray // fallback
    }
}

