package com.bizsync.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer

@Composable
fun UniversalCard(
    loading: Boolean = false,
    title: String = "",
    subtitle: String = "",
    showDelete: Boolean = false,
    onDelete: () -> Unit = {},
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            if (content == null) {
                // Modalità standard con titolo e sottotitolo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (loading) "" else title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .placeholder(
                                    visible = loading,
                                    highlight = PlaceholderHighlight.shimmer()
                                ),
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (subtitle.isNotBlank() || loading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (loading) "" else subtitle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .placeholder(
                                        visible = loading,
                                        highlight = PlaceholderHighlight.shimmer()
                                    ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (showDelete && !loading) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Rimuovi",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                // Modalità custom content
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer()
                            )
                    )
                } else {
                    content()
                }
            }
        }
    }
}