package com.bizsync.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.EmployeeAvatar
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.viewmodels.EmployeeManagementViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(
    onBackClick: () -> Unit = {},
) {


    val employeeVM : EmployeeManagementViewModel = hiltViewModel()


    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()

    val uiState by employeeVM.uiState.collectAsState()


    val searchQuery = uiState.searchQuery
    val selectedDepartment = uiState.selectedDepartment
    val employees = uiState.employees
    val isLoading = uiState.isLoading

    LaunchedEffect(Unit) {
        employeeVM.loadEmployees()
    }


    val departments = listOf(AreaLavoro(nomeArea = "Tutti")) + userState.azienda.areeLavoro

    val selectedEmployee = uiState.selectedEmployee

    val filteredEmployees = employees.filter { employee ->
        val matchesSearch = employee.nome.contains(searchQuery, ignoreCase = true) ||
                employee.cognome.contains(searchQuery, ignoreCase = true) ||
                employee.posizioneLavorativa.contains(searchQuery, ignoreCase = true)
        val matchesDepartment = selectedDepartment == "Tutti" || employee.dipartimento == selectedDepartment
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        )

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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { employeeVM.updateSearchQuery(it) },
                    label = { Text("Cerca dipendente...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cerca")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments) { department ->
                        FilterChip(
                            onClick = {  employeeVM.updateSelectedDepartment(department.nomeArea) },
                            label = { Text(department.nomeArea) },
                            selected = selectedDepartment == department.nomeArea,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3498DB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEmployees) { employee ->
                EmployeeCard(
                    employee = employee,
                    onClick = { employeeVM.updateSelectedEmployee(employee) }
                )
            }

            if (filteredEmployees.isEmpty() && !isLoading) {
                item {
                    EmptyStateCard()
                }
            }
        }
    }

    selectedEmployee?.let { employee ->
        EmployeeDetailScreen(
            employee = employee,
            employeeVm = employeeVM
        )
    }

}

@Composable
fun EmployeeCard(
    employee: UserUi,
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
            EmployeeAvatar(
                photoUrl = employee.photourl,
                nome = employee.nome,
                cognome = employee.cognome
            )


            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${employee.nome} ${employee.cognome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = employee.posizioneLavorativa,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = employee.dipartimento,
                    fontSize = 12.sp,
                    color = Color(0xFF95A5A6)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
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