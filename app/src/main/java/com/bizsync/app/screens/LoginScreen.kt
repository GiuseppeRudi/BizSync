package com.bizsync.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(onLoginScreen : () -> Unit)
{


    Column {
        Text(text = "Benvenuto su BizSync ")

        Text(text = " Se sei nuovo effettua il login ")

        Button(onClick = onLoginScreen) { }

        Text(text = " Altrimenti registrati  ")
    }

}



@Preview
@Composable
private fun LoginPreview(){

    LoginScreen(onLoginScreen = { "ciao"})
}

