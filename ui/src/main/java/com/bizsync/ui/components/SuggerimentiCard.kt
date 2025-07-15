//package com.bizsync.ui.components
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Lightbulb
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bizsync.ui.viewmodels.SuggerimentoTurno
//import kotlin.collections.forEach
//
//@Composable
//fun SuggerimentiCard(
//    suggerimenti: List<SuggerimentoTurno>,
//    onApplySuggerimento: (SuggerimentoTurno) -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.tertiaryContainer
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    Icons.Default.Lightbulb,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onTertiaryContainer
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Suggerimenti",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onTertiaryContainer
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            suggerimenti.forEach { suggerimento ->
//                SuggerimentoItem(
//                    suggerimento = suggerimento,
//                    onApply = { onApplySuggerimento(suggerimento) }
//                )
//                if (suggerimento != suggerimenti.last()) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SuggerimentoItem(
//    suggerimento: SuggerimentoTurno,
//    onApply: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                text = suggerimento.titolo,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onTertiaryContainer
//            )
//            Text(
//                text = suggerimento.descrizione,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onTertiaryContainer
//            )
//        }
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        TextButton(
//            onClick = onApply,
//            colors = ButtonDefaults.textButtonColors(
//                contentColor = MaterialTheme.colorScheme.primary
//            )
//        ) {
//            Text("Applica")
//        }
//    }
//}