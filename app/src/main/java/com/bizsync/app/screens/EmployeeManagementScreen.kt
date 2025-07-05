package com.bizsync.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.navigation.LocalUserViewModel


// Data class per rappresentare un dipendente
data class Employee(
    val id: String,
    val name: String,
    val surname: String,
    val position: String,
    val department: String,
    val email: String,
    val phone: String,
    val status: EmployeeStatus,
    val avatarUrl: String? = null,
    val hireDate: String,
    val salary: Double? = null
)

enum class EmployeeStatus(val displayName: String, val color: Color) {
    ACTIVE("Attivo", Color(0xFF4CAF50)),
    ON_LEAVE("In Permesso", Color(0xFFFF9800)),
    INACTIVE("Inattivo", Color(0xFFF44336))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("Tutti") }
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

    val userVm = LocalUserViewModel.current

    // Dati di esempio per i dipendenti
    val sampleEmployees = remember {
        listOf(
            Employee(
                id = "1",
                name = "Marco",
                surname = "Rossi",
                position = "Sviluppatore Senior",
                department = "IT",
                email = "marco.rossi@bizsync.com",
                phone = "+39 123 456 7890",
                status = EmployeeStatus.ACTIVE,
                hireDate = "15/01/2022",
                salary = 45000.0
            ),
            Employee(
                id = "2",
                name = "Giulia",
                surname = "Bianchi",
                position = "Project Manager",
                department = "Gestione Progetti",
                email = "giulia.bianchi@bizsync.com",
                phone = "+39 123 456 7891",
                status = EmployeeStatus.ACTIVE,
                hireDate = "03/05/2021",
                salary = 42000.0
            ),
            Employee(
                id = "3",
                name = "Andrea",
                surname = "Verdi",
                position = "Designer UX/UI",
                department = "Design",
                email = "andrea.verdi@bizsync.com",
                phone = "+39 123 456 7892",
                status = EmployeeStatus.ON_LEAVE,
                hireDate = "20/09/2022",
                salary = 38000.0
            ),
            Employee(
                id = "4",
                name = "Sara",
                surname = "Neri",
                position = "Responsabile HR",
                department = "Risorse Umane",
                email = "sara.neri@bizsync.com",
                phone = "+39 123 456 7893",
                status = EmployeeStatus.ACTIVE,
                hireDate = "10/03/2020",
                salary = 40000.0
            ),
            Employee(
                id = "5",
                name = "Luca",
                surname = "Gialli",
                position = "Analista Finanziario",
                department = "Finanze",
                email = "luca.gialli@bizsync.com",
                phone = "+39 123 456 7894",
                status = EmployeeStatus.INACTIVE,
                hireDate = "05/11/2023",
                salary = 35000.0
            )
        )
    }

    var addMakeInvite: Boolean by remember { mutableStateOf(false) }

    val departments = remember {
        listOf("Tutti") + sampleEmployees.map { it.department }.distinct()
    }

    val filteredEmployees = sampleEmployees.filter { employee ->
        val matchesSearch = employee.name.contains(searchQuery, ignoreCase = true) ||
                employee.surname.contains(searchQuery, ignoreCase = true) ||
                employee.position.contains(searchQuery, ignoreCase = true)
        val matchesDepartment = selectedDepartment == "Tutti" || employee.department == selectedDepartment
        matchesSearch && matchesDepartment
    }

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
        // Header con bottone indietro
        TopAppBar(
            title = {
                Text(
                    text = "Gestione Dipendenti",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { addMakeInvite = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Aggiungi dipendente",
                        tint = Color(0xFF3498DB)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (addMakeInvite)
        {
            // Dialog per inviti
            MakeInviteScreen(
                onNavigateBack = { addMakeInvite = false },
                userVm
            )

        }

        // Stats cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            item {
                StatsCard(
                    title = "Totale",
                    value = sampleEmployees.size.toString(),
                    color = Color(0xFF3498DB)
                )
            }
            item {
                StatsCard(
                    title = "Attivi",
                    value = sampleEmployees.count { it.status == EmployeeStatus.ACTIVE }.toString(),
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                StatsCard(
                    title = "In Permesso",
                    value = sampleEmployees.count { it.status == EmployeeStatus.ON_LEAVE }.toString(),
                    color = Color(0xFFFF9800)
                )
            }
            item {
                StatsCard(
                    title = "Inattivi",
                    value = sampleEmployees.count { it.status == EmployeeStatus.INACTIVE }.toString(),
                    color = Color(0xFFF44336)
                )
            }
        }

        // Filtri
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Barra di ricerca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cerca dipendente...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cerca")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Filtro dipartimento
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments) { department ->
                        FilterChip(
                            onClick = { selectedDepartment = department },
                            label = { Text(department) },
                            selected = selectedDepartment == department,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3498DB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Lista dipendenti
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEmployees) { employee ->
                EmployeeCard(
                    employee = employee,
                    onClick = { selectedEmployee = employee }
                )
            }

            if (filteredEmployees.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            }
        }
    }

    // Dialog dettagli dipendente
    selectedEmployee?.let { employee ->
        EmployeeDetailDialog(
            employee = employee,
            onDismiss = { selectedEmployee = null }
        )
    }

}

@Composable
fun StatsCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF7F8C8D)
            )
        }
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${employee.name.first()}${employee.surname.first()}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info dipendente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${employee.name} ${employee.surname}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = employee.position,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = employee.department,
                    fontSize = 12.sp,
                    color = Color(0xFF95A5A6)
                )
            }

            // Status badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = employee.status.color.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = employee.status.displayName,
                    fontSize = 10.sp,
                    color = employee.status.color,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EmployeeDetailDialog(
    employee: Employee,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${employee.name.first()}${employee.surname.first()}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("${employee.name} ${employee.surname}")
            }
        },
        text = {
            Column {
                DetailRow("Posizione", employee.position)
                DetailRow("Dipartimento", employee.department)
                DetailRow("Email", employee.email)
                DetailRow("Telefono", employee.phone)
                DetailRow("Data Assunzione", employee.hireDate)
                employee.salary?.let {
                    DetailRow("Stipendio", "€${String.format("%.2f", it)}")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = employee.status.color.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = employee.status.displayName,
                            fontSize = 12.sp,
                            color = employee.status.color,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDC3C7)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nessun dipendente trovato",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7F8C8D)
            )
            Text(
                text = "Prova a modificare i filtri di ricerca",
                fontSize = 14.sp,
                color = Color(0xFFBDC3C7)
            )
        }
    }
}