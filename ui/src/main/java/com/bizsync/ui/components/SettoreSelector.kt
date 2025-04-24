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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bizsync.ui.viewmodels.WelcomeViewModel


@Composable
fun SettoreSelector(welcomeviewmodel: WelcomeViewModel)
{
    val settori = listOf("Informatica", "Edilizia" , "Commercio" , "Sanità", "Turismo" , "Altro")
    val selectedSettore = welcomeviewmodel.settore
    val customSettore = welcomeviewmodel.customSettore
    val isCustom = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()){
        settori.forEach { settore ->
            Card( modifier = Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable{
                    selectedSettore.value = settore
                    isCustom.value = settore == "Altro"}
                ,
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedSettore.value == settore) Color.Black
                    else MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            )
            {
                Text(
                    text = settore,
                    modifier = Modifier.padding(16.dp),
                    color = if (selectedSettore.value == settore) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if(isCustom.value){
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedSettore.value,
                onValueChange = {selectedSettore.value= it},
                label = { Text("Inserisci il tuo settore")},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

}