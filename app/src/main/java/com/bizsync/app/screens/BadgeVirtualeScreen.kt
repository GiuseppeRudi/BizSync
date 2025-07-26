package com.bizsync.app.screens


import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.ui.viewmodels.HomeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BadgeVirtualeScreen(modifier: Modifier = Modifier, viewModel: HomeViewModel) {
    var showQRCode by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val badge = uiState.badge

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.setBadge(userState)
    }

    if (badge == null) {
        LoadingIndicator()
    }
    else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        ) {
            // Back button
            IconButton(
                onClick = { viewModel.changeCurrentScreen(HomeScreenRoute.Home) },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo azienda
                            Spacer(modifier = Modifier.height(40.dp))

                            // Foto profilo con bordo
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 4.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .shadow(8.dp, CircleShape)
                            ) {
                                AsyncImage(
                                    model = badge.fotoUrl,
                                    contentDescription = "Foto profilo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Nome e cognome
                            Text(
                                text = badge.getFullName(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Posizione lavorativa
                            Text(
                                text = badge.posizioneLavorativa,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )


                            Spacer(modifier = Modifier.height(20.dp))

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoColumn(
                                    label = "MATRICOLA",
                                    value = badge.matricola
                                )

                                VerticalDivider(
                                    modifier = Modifier.height(50.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                InfoColumn(
                                    label = "AZIENDA",
                                    value = badge.nomeAzienda
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // QR Code section
                            AnimatedContent(
                                targetState = showQRCode,
                                transitionSpec = {
                                    fadeIn() + expandVertically() with
                                            fadeOut() + shrinkVertically()
                                }
                            ) { showQR ->
                                if (showQR) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        QRCodeImage(
                                            data = badge.generateQRData(),
                                            modifier = Modifier.size(150.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Scansiona per verificare",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { showQRCode = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("MOSTRA QR CODE")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Data e ora corrente
                            CurrentDateTime()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Badge digitale verificato e sicuro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun InfoColumn(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CurrentDateTime() {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun QRCodeImage(data: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val qrBitmap by remember(data) {
        mutableStateOf(generateQRCodeBitmap(data).asImageBitmap())
    }

    Image(
        bitmap = qrBitmap,
        contentDescription = "QR Code",
        modifier = modifier
    )
}


fun generateQRCodeBitmap(text: String, size: Int = 512): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap[x, y] =
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }

    return bitmap
}


