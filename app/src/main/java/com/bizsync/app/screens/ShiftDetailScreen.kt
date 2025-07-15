package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.model.Turno
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.viewmodels.EmployeeViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(
    employee: UserUi,
    employeeVm : EmployeeViewModel,
    isHistorical: Boolean = false,
) {

    val uiState by employeeVm.uiState.collectAsState()
//    val shifts = uiState.shifts
    val isLoading = uiState.isLoading


//    LaunchedEffect(employee.uid, isHistorical) {
//        if (isHistorical) {
//            employeeVm.loadEmployeePastShifts(employee.uid)
//        } else {
//            employeeVm.loadEmployeeFutureShifts(employee.uid)
//        }
//    }

//    val exampleShifts = listOf(
//        Turno(
//            idDocumento = "1",
//            nome = "Mattina",
//            giorno = Timestamp(Date(System.currentTimeMillis()))
//        ),
//        Turno(
//            idDocumento = "2",
//            nome = "Pomeriggio",
//            giorno = Timestamp(Date(System.currentTimeMillis() + 86400000L)) // +1 giorno
//        ),
//        Turno(
//            idDocumento = "3",
//            nome = "Notte",
//            giorno = Timestamp(Date(System.currentTimeMillis() + 2 * 86400000L)) // +2 giorni
//        )
//    )

    val exampleShifts = emptyList<Turno>()
    val shifts = exampleShifts


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = if (isHistorical) "Turni Passati" else "Turni Futuri",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            },
            navigationIcon = {
                IconButton(onClick = {employeeVm.setCurrentSection(EmployeeSection.MAIN) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        )

        // Sottotitolo con nome dipendente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                            else Color(0xFFF39C12).copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHistorical) Icons.Default.History else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${employee.nome} ${employee.cognome}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        text = employee.posizioneLavorativa,
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12)
                )
            }
        } else if (shifts.isEmpty()) {
            EmptyShiftsState(isHistorical)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shifts) { shift ->
                    ShiftCard(
                        shift = shift,
                        isHistorical = isHistorical
                    )
                }
            }
        }
    }
}

@Composable
fun ShiftCard(
    shift: Turno,
    isHistorical: Boolean
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(shift.data)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona del turno
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                        else Color(0xFFF39C12).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informazioni del turno
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift.titolo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Badge stato
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isHistorical) Color(0xFF2ECC71).copy(alpha = 0.1f)
                    else Color(0xFFF39C12).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isHistorical) "Completato" else "Programmato",
                    fontSize = 12.sp,
                    color = if (isHistorical) Color(0xFF2ECC71) else Color(0xFFF39C12),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyShiftsState(isHistorical: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isHistorical) Icons.Default.History else Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDC3C7)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHistorical) "Nessun turno passato" else "Nessun turno programmato",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7F8C8D)
            )
            Text(
                text = if (isHistorical) "Non ci sono turni completati da mostrare"
                else "Non ci sono turni futuri programmati",
                fontSize = 14.sp,
                color = Color(0xFFBDC3C7),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}