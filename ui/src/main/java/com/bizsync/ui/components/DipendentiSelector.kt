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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import com.bizsync.ui.viewmodels.AddAziendaViewModel

@Composable
fun DipendentiSelector(addaziendaviewmodel: AddAziendaViewModel)
{
    val ranges = listOf("0 - 20", "21 - 40" , "41 - 60", "61 - 100" , " 100+ ")
    val uiState by addaziendaviewmodel.uiState.collectAsState()
    val selectedRange = uiState.azienda.numDipendentiRange

    Column(modifier = Modifier.fillMaxWidth())
    {
        ranges.forEach { range ->

            Card(modifier = Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { addaziendaviewmodel.onNumDipendentiRangeChanged(range)},

                colors = CardDefaults.cardColors( containerColor = if (selectedRange == range) MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant),

                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )      {

                Text( text = range,
                    modifier = Modifier.padding(16.dp),
                    color = if (selectedRange == range) Color.White else Color.Black)
            }
        }
    }
}