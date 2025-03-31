package com.bizsync.app.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bizsync.app.Calendar


@Composable
fun HomeScreen(navController: NavController) {
    // Usa remember con mutableStateOf per tenere traccia del valore della variabile message
    val message = remember { mutableStateOf("ciao") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Calendar()

    }
}



@Composable
fun DetailsScreen(){

    Text(text = "Sezione dei dettagli del sito", fontSize = 24.sp, modifier = Modifier.padding(50.dp))
}




