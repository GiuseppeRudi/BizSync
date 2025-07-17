package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.model.User


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPrivateChatDialog(
    currentUser: User,
    employees: List<User>,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova chat privata") },
        text = {
            LazyColumn {
                items(employees) { employee ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUserSelected(employee) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorPalette.random()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = employee.nome.first().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${employee.nome} ${employee.cognome}",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${employee.posizioneLavorativa} - ${employee.dipartimento}",
                                fontSize = 12.sp,
                                color = Color(0xFF7F8C8D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

val colorPalette = listOf(
    Color(0xFFEF9A9A), // rosa
    Color(0xFF90CAF9), // blu chiaro
    Color(0xFFA5D6A7), // verde chiaro
    Color(0xFFFFF59D), // giallo chiaro
    Color(0xFFCE93D8), // viola chiaro
    Color(0xFFFFCC80), // arancio chiaro
    Color(0xFF80CBC4), // turchese chiaro
    Color(0xFFB0BEC5)  // grigio chiaro
)
