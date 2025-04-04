package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Sfondo nero per contrasto
        contentAlignment = Alignment.Center
    ) {
        // Animazione di visibilità
        AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(1000))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logobizsync),
                    contentDescription = "Logo",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "BizSync",
                    color = Color.White, // Testo bianco per contrasto
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Splash")
@Composable
private fun SplashPreview() {
    MaterialTheme {
        SplashScreen()
    }
}
