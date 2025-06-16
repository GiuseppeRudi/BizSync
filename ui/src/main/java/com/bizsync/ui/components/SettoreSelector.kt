package com.bizsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import com.bizsync.ui.viewmodels.AddAziendaViewModel


@Composable
fun SettoreSelector(addaziendaviewmodel: AddAziendaViewModel)
{
    val settori = listOf("Informatica", "Edilizia" , "Commercio" , "Sanità", "Turismo" , "Altro")
    val uiState by addaziendaviewmodel.uiState.collectAsState()
    val selectedSettore = uiState.azienda.sector
    val isCustom = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()){
        settori.forEach { settore ->
            Card( modifier = Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable{
                    addaziendaviewmodel.onSectorChanged(settore)
                    isCustom.value = settore == "Altro"}
                ,
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedSettore == settore) Color.Black
                    else MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )
            {
                Text(
                    text = settore,
                    modifier = Modifier.padding(16.dp),
                    color = if (selectedSettore == settore) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if(isCustom.value){
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedSettore,
                onValueChange = {addaziendaviewmodel.onSectorChanged(it)},
                label = { Text("Inserisci il tuo settore")},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

}