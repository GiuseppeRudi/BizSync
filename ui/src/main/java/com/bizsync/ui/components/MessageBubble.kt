package com.bizsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Nome del mittente (solo se non è l'utente corrente)
            if (!isFromCurrentUser) {
                Text(
                    text = message.senderNome,
                    color = Color(0xFF7F8C8D),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = if (isFromCurrentUser) 18.dp else 4.dp,
                    topEnd = if (isFromCurrentUser) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when (message.tipo) {
                        MessageType.ANNOUNCEMENT -> Color(0xFFFFF3CD)
                        MessageType.SYSTEM -> Color(0xFFD4EDDA)
                        MessageType.TEXT -> if (isFromCurrentUser) Color(0xFF3498DB) else Color.White
                        else -> {  Color.White }
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Icona per annunci
                    if (message.tipo == MessageType.ANNOUNCEMENT) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFF39C12),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ANNUNCIO",
                                color = Color(0xFFF39C12),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Categoria (se presente)
                    message.categoria?.let { categoria ->
                        Text(
                            text = categoria.uppercase(),
                            color = Color(0xFF7F8C8D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.content,
                        color = when (message.tipo) {
                            MessageType.ANNOUNCEMENT -> Color(0xFF856404)
                            MessageType.SYSTEM -> Color(0xFF155724)
                            MessageType.TEXT -> if (isFromCurrentUser) Color.White else Color(0xFF2C3E50)
                            else -> { Color.White }
                        },
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .width(80.dp)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
            ) {
                Text(
                    text = timeFormat.format(message.timestamp),
                    color = Color(0xFF95A5A6),
                    fontSize = 12.sp
                )

                if (isFromCurrentUser && message.isLetto) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Letto",
                        tint = Color(0xFF3498DB),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}