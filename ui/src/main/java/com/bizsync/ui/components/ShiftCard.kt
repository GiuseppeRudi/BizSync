package com.bizsync.ui.components

import android.R.attr.visible
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer

@Composable
fun ShiftCard(loading: Boolean, title: String = "", time: String = "") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Text(
                text = if (loading) "" else title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp) // per mostrare bene lo shimmer
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (loading) "" else time,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp) // altezza fissa anche qui
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
