package com.bizsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bizsync.ui.viewmodels.WelcomeViewModel

@Composable
fun DipendentiSelector(welcomeviewmodel: WelcomeViewModel)
{
    val ranges = listOf("0 - 20", "21 - 40" , "41 - 60", "61 - 100" , " 100+ ")
    val selectedRange = welcomeviewmodel.numDipendentiRange

    Column(modifier = Modifier.fillMaxWidth())
    {
        ranges.forEach { range ->

            Card(modifier = Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { selectedRange.value = range},

                colors = CardDefaults.cardColors( containerColor = if (selectedRange.value == range) MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant),

                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )      {

                Text( text = range,
                    modifier = Modifier.padding(16.dp),
                    color = if (selectedRange.value == range) Color.White else Color.Black)
            }
        }
    }
}