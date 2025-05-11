import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginScreen: () -> Unit,
    lastUserEmail: String? = null,
    onLoginWithLastAccount: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Benvenuto su",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )

            Text(
                text = "BizSync",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Text(
                text = "Il nuovo modo di vivere e gestire l'azienda,\nsmart e in tempo reale.",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            // Se abbiamo un account precedente, mostriamo l'opzione per usarlo
            if (lastUserEmail != null) {
                Button(
                    onClick = onLoginWithLastAccount,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Accedi con",
                            fontSize = 14.sp
                        )
                        Text(
                            text = lastUserEmail,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "oppure",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = onLoginScreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (lastUserEmail == null) "Entra" else "Accedi con un altro account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginPreview(){
    LoginScreen(
        onLoginScreen = { },
        lastUserEmail = "utente@gmail.com",
        onLoginWithLastAccount = { }
    )
}

@Preview
@Composable
private fun LoginPreviewNoLastUser(){
    LoginScreen(
        onLoginScreen = { },
        lastUserEmail = null,
        onLoginWithLastAccount = { }
    )
}
