package com.bizsync.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.model.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*

// Data classes per i report
data class ReportData(
    val contratti: List<Contratto>,
    val users: List<User>,
    val absences: List<Absence>,
    val turni: List<Turno>
)

data class DepartmentStats(
    val name: String,
    val employeeCount: Int,
    val totalSalary: Double,
    val avgFerieUsed: Double,
    val avgRolUsed: Double,
    val color: Color
)

data class AbsenceStats(
    val type: AbsenceType,
    val count: Int,
    val percentage: Float,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsManagementScreen(
    contratti: List<Contratto>,
    users: List<User>,
    absences: List<Absence>,
    turni: List<Turno>,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ReportFilter.ALL_TIME) }
    var selectedDepartment by remember { mutableStateOf("Tutti") }
    var selectedTab by remember { mutableStateOf(0) }

    val reportData = remember(contratti, users, absences, turni) {
        ReportData(contratti, users, absences, turni)
    }

    val departments = remember(users) {
        listOf("Tutti") + users.map { it.dipartimento }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard Aziendale",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Report e Analisi",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Esporta")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Filtri
            FilterSection(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                selectedDepartment = selectedDepartment,
                departments = departments,
                onDepartmentChange = { selectedDepartment = it }
            )

            // Tab per diverse sezioni
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("HR Analytics") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Turni") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Costi") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenuto basato sul tab selezionato
            when (selectedTab) {
                0 -> OverviewTab(reportData, selectedFilter, selectedDepartment)
                1 -> HRAnalyticsTab(reportData, selectedFilter, selectedDepartment)
                2 -> ShiftsTab(reportData, selectedFilter, selectedDepartment)
                3 -> CostsTab(reportData, selectedFilter, selectedDepartment)
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedFilter: ReportFilter,
    onFilterChange: (ReportFilter) -> Unit,
    selectedDepartment: String,
    departments: List<String>,
    onDepartmentChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtri",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro periodo
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReportFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtro dipartimento
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(departments) { dept ->
                    FilterChip(
                        selected = selectedDepartment == dept,
                        onClick = { onDepartmentChange(dept) },
                        label = { Text(dept) },
                        leadingIcon = if (selectedDepartment == dept) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    reportData: ReportData,
    filter: ReportFilter,
    department: String
) {
    val filteredData = filterData(reportData, filter, department)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            item {
                KPICard(
                    title = "Dipendenti Totali",
                    value = filteredData.users.size.toString(),
                    icon = Icons.Default.People,
                    color = Color(0xFF3498DB),
                    trend = "+5%"
                )
            }
            item {
                KPICard(
                    title = "Assenze Oggi",
                    value = filteredData.absences.count {
                        it.startDate <= LocalDate.now() && it.endDate >= LocalDate.now()
                    }.toString(),
                    icon = Icons.Default.EventBusy,
                    color = Color(0xFFE74C3C),
                    trend = "-2%"
                )
            }
            item {
                KPICard(
                    title = "Ore Lavorate",
                    value = calculateTotalHours(filteredData.turni).toString(),
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF2ECC71),
                    trend = "+8%"
                )
            }
            item {
                KPICard(
                    title = "Costo Mensile",
                    value = "€${formatCurrency(calculateMonthlyCost(filteredData.contratti))}",
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFFF39C12),
                    trend = "0%"
                )
            }
        }

        // Grafico distribuzione dipendenti per dipartimento
        DepartmentDistributionChart(
            users = filteredData.users,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        // Grafico assenze per tipo
        AbsenceTypeChart(
            absences = filteredData.absences,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
fun HRAnalyticsTab(
    reportData: ReportData,
    filter: ReportFilter,
    department: String
) {
    val filteredData = filterData(reportData, filter, department)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Utilizzo Ferie/ROL
        LeaveUsageChart(
            contratti = filteredData.contratti,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Trend assenze mensili
        MonthlyAbsenceTrend(
            absences = filteredData.absences,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        // Top dipendenti per assenze
        TopEmployeesAbsences(
            absences = filteredData.absences,
            users = filteredData.users,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShiftsTab(
    reportData: ReportData,
    filter: ReportFilter,
    department: String
) {
    val filteredData = filterData(reportData, filter, department)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Distribuzione turni per dipartimento
        ShiftDistributionChart(
            turni = filteredData.turni,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Heatmap turni settimanali
        WeeklyShiftHeatmap(
            turni = filteredData.turni,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Lista turni di oggi
        TodayShiftsList(
            turni = filteredData.turni.filter { it.data == LocalDate.now() },
            users = filteredData.users,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CostsTab(
    reportData: ReportData,
    filter: ReportFilter,
    department: String
) {
    val filteredData = filterData(reportData, filter, department)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Costi per dipartimento
        DepartmentCostChart(
            contratti = filteredData.contratti,
            users = filteredData.users,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Breakdown costi
        CostBreakdownCard(
            contratti = filteredData.contratti,
            modifier = Modifier.fillMaxWidth()
        )

        // Proiezione costi annuali
        YearlyCostProjection(
            contratti = filteredData.contratti,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

// Componenti grafici

@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (trend.startsWith("+")) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (trend.startsWith("+")) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = trend,
                        fontSize = 12.sp,
                        color = if (trend.startsWith("+")) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DepartmentDistributionChart(
    users: List<User>,
    modifier: Modifier = Modifier
) {
    val departmentCounts = users.groupBy { it.dipartimento }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    val colors = listOf(
        Color(0xFF3498DB),
        Color(0xFF9B59B6),
        Color(0xFFE74C3C),
        Color(0xFF2ECC71),
        Color(0xFFF39C12),
        Color(0xFF1ABC9C)
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Distribuzione Dipendenti per Dipartimento",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val total = departmentCounts.sumOf { it.second }.toFloat()
                var startAngle = -90f

                departmentCounts.forEachIndexed { index, (dept, count) ->
                    val sweepAngle = (count / total) * 360f
                    val color = colors[index % colors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = size.copy(width = size.minDimension, height = size.minDimension)
                    )

                    startAngle += sweepAngle
                }

                // Centro bianco
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legenda
            departmentCounts.forEachIndexed { index, (dept, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dept,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$count (${((count.toFloat() / users.size) * 100).toInt()}%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AbsenceTypeChart(
    absences: List<Absence>,
    modifier: Modifier = Modifier
) {
    val typeColors = mapOf(
        AbsenceType.VACATION to Color(0xFF3498DB),
        AbsenceType.SICK_LEAVE to Color(0xFFE74C3C),
        AbsenceType.UNPAID_LEAVE to Color(0xFFF39C12),
        AbsenceType.ROL to Color(0xFF9B59B6)
    )

    val typeCounts = absences.groupBy { it.type }
        .mapValues { it.value.size }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tipologie di Assenze",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            typeCounts.forEach { (type, count) ->
                val percentage = if (absences.isNotEmpty()) {
                    (count.toFloat() / absences.size) * 100
                } else 0f

                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = type.name,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$count (${percentage.toInt()}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = typeColors[type] ?: Color.Gray,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveUsageChart(
    contratti: List<Contratto>,
    modifier: Modifier = Modifier
) {
    val avgUsage = contratti.map { contratto ->
        val feriePercentage = if (contratto.ccnlInfo.ferieAnnue > 0) {
            (contratto.ferieUsate.toFloat() / contratto.ccnlInfo.ferieAnnue) * 100
        } else 0f

        val rolPercentage = if (contratto.ccnlInfo.rolAnnui > 0) {
            (contratto.rolUsate.toFloat() / contratto.ccnlInfo.rolAnnui) * 100
        } else 0f

        Triple(contratto.dipartimento, feriePercentage, rolPercentage)
    }.groupBy { it.first }
        .mapValues { entry ->
            val ferie = entry.value.map { it.second }.average().toFloat()
            val rol = entry.value.map { it.third }.average().toFloat()
            Pair(ferie, rol)
        }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Utilizzo Medio Ferie e ROL per Dipartimento",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            avgUsage.forEach { (dept, usage) ->
                Column(
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = dept,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ferie
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ferie",
                            fontSize = 12.sp,
                            modifier = Modifier.width(50.dp)
                        )
                        LinearProgressIndicator(
                            progress = usage.first / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF3498DB),
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${usage.first.toInt()}%",
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ROL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROL",
                            fontSize = 12.sp,
                            modifier = Modifier.width(50.dp)
                        )
                        LinearProgressIndicator(
                            progress = usage.second / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF9B59B6),
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${usage.second.toInt()}%",
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyAbsenceTrend(
    absences: List<Absence>,
    modifier: Modifier = Modifier
) {
    val monthlyData = absences.groupBy {
        YearMonth.from(it.startDate)
    }.mapValues { it.value.size }
        .toList()
        .sortedBy { it.first }
        .takeLast(6)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trend Assenze Mensili",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (monthlyData.isNotEmpty()) {
                val maxValue = monthlyData.maxOf { it.second }.toFloat()

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val barWidth = size.width / (monthlyData.size * 1.5f)
                    val spacing = barWidth * 0.5f

                    monthlyData.forEachIndexed { index, (month, count) ->
                        val barHeight = (count / maxValue) * size.height * 0.8f
                        val x = index * (barWidth + spacing) + spacing

                        // Barra
                        drawRoundRect(
                            color = Color(0xFF3498DB),
                            topLeft = Offset(x, size.height - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }

                // Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    monthlyData.forEach { (month, count) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = month.format(DateTimeFormatter.ofPattern("MMM")),
                                fontSize = 10.sp
                            )
                            Text(
                                text = count.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentCostChart(
    contratti: List<Contratto>,
    users: List<User>,
    modifier: Modifier = Modifier
) {
    val departmentCosts = contratti.groupBy { contratto ->
        users.find { it.uid == contratto.idDipendente }?.dipartimento ?: "Unknown"
    }.mapValues { entry ->
        entry.value.sumOf { it.ccnlInfo.stipendioAnnualeLordo } / 12.0
    }.toList().sortedByDescending { it.second }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Costi Mensili per Dipartimento",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            departmentCosts.forEach { (dept, cost) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dept,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "€${formatCurrency(cost)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF39C12)
                        )
                    }

                    // Mini grafico a barre
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                        val maxCost = departmentCosts.maxOf { it.second }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((cost / maxCost).toFloat())
                                .background(Color(0xFFF39C12))
                        )
                    }
                }
            }
        }
    }
}

// Utility functions

fun filterData(
    data: ReportData,
    filter: ReportFilter,
    department: String
): ReportData {
    val now = LocalDate.now()
    val startDate = when (filter) {
        ReportFilter.TODAY -> now
        ReportFilter.WEEK -> now.minusWeeks(1)
        ReportFilter.MONTH -> now.minusMonths(1)
        ReportFilter.QUARTER -> now.minusMonths(3)
        ReportFilter.YEAR -> now.minusYears(1)
        ReportFilter.ALL_TIME -> LocalDate.MIN
    }

    val filteredUsers = if (department == "Tutti") {
        data.users
    } else {
        data.users.filter { it.dipartimento == department }
    }

    val userIds = filteredUsers.map { it.uid }.toSet()

    return ReportData(
        contratti = data.contratti.filter { it.idDipendente in userIds },
        users = filteredUsers,
        absences = data.absences.filter {
            it.idUser in userIds && it.startDate >= startDate
        },
        turni = data.turni.filter { turno ->
            turno.data >= startDate &&
                    turno.idDipendenti.any { it in userIds }
        }
    )
}

fun calculateTotalHours(turni: List<Turno>): Int {
    return turni.sumOf { it.calcolaDurata() }
}

fun calculateMonthlyCost(contratti: List<Contratto>): Double {
    return contratti.sumOf { it.ccnlInfo.stipendioAnnualeLordo } / 12.0
}

fun formatCurrency(amount: Double): String {
    return DecimalFormat("#,##0").format(amount)
}

enum class ReportFilter(val label: String) {
    TODAY("Oggi"),
    WEEK("Settimana"),
    MONTH("Mese"),
    QUARTER("Trimestre"),
    YEAR("Anno"),
    ALL_TIME("Tutto")
}

// Altri componenti supplementari

@Composable
fun TopEmployeesAbsences(
    absences: List<Absence>,
    users: List<User>,
    modifier: Modifier = Modifier
) {
    val topAbsences = absences.groupBy { it.idUser }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top 5 Dipendenti per Assenze",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            topAbsences.forEach { (userId, count) ->
                val user = users.find { it.uid == userId }
                user?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${it.nome} ${it.cognome}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = it.dipartimento,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Badge(
                            containerColor = Color(0xFFE74C3C).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = count.toString(),
                                color = Color(0xFFE74C3C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CostBreakdownCard(
    contratti: List<Contratto>,
    modifier: Modifier = Modifier
) {
    val totalCost = contratti.sumOf { it.ccnlInfo.stipendioAnnualeLordo }
    val avgCost = if (contratti.isNotEmpty()) totalCost / contratti.size else 0
    val minCost = contratti.minOfOrNull { it.ccnlInfo.stipendioAnnualeLordo } ?: 0
    val maxCost = contratti.maxOfOrNull { it.ccnlInfo.stipendioAnnualeLordo } ?: 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Breakdown Costi Annuali",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Totale",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "€${formatCurrency(totalCost.toDouble())}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF39C12)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Media",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "€${formatCurrency(avgCost.toDouble())}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3498DB)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Min/Max",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "€${formatCurrency(minCost.toDouble())} - €${formatCurrency(maxCost.toDouble())}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2ECC71)
                    )
                }
            }
        }
    }
}

@Composable
fun ShiftDistributionChart(
    turni: List<Turno>,
    modifier: Modifier = Modifier
) {
    val shiftsByDept = turni.groupBy { it.dipartimentoId }
        .mapValues { it.value.size }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Distribuzione Turni per Dipartimento",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (shiftsByDept.isNotEmpty()) {
                val maxShifts = shiftsByDept.maxOf { it.value }.toFloat()

                shiftsByDept.forEach { (dept, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dept,
                            fontSize = 14.sp,
                            modifier = Modifier.width(100.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((count / maxShifts))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF3498DB),
                                                Color(0xFF2ECC71)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = count.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyShiftHeatmap(
    turni: List<Turno>,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
    val currentWeek = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Heatmap Turni Settimanali",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    val date = currentWeek.plusDays(index.toLong())
                    val shiftsCount = turni.count { it.data == date }
                    val intensity = (shiftsCount / 10f).coerceIn(0f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Color(0xFF3498DB).copy(alpha = 0.2f + (intensity * 0.8f))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shiftsCount.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (intensity > 0.5f) Color.White else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = day,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodayShiftsList(
    turni: List<Turno>,
    users: List<User>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Turni di Oggi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Badge {
                    Text(text = turni.size.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (turni.isEmpty()) {
                Text(
                    text = "Nessun turno programmato per oggi",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                turni.take(5).forEach { turno ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = turno.titolo,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${turno.orarioInizio.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${turno.orarioFine.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = turno.dipartimentoId,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${turno.idDipendenti.size} dip.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearlyCostProjection(
    contratti: List<Contratto>,
    modifier: Modifier = Modifier
) {
    val currentMonth = LocalDate.now().monthValue
    val monthlyProjections = (1..12).map { month ->
        val monthlyBase = contratti.sumOf { it.ccnlInfo.stipendioAnnualeLordo } / 12.0
        val projection = if (month <= currentMonth) {
            monthlyBase // Mesi passati: costo reale
        } else {
            // Proiezione con crescita stimata del 2%
            monthlyBase * (1 + 0.02 * (month - currentMonth) / 12)
        }
        Pair(month, projection)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Proiezione Costi Annuali",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val maxValue = monthlyProjections.maxOf { it.second }.toFloat()
                val minValue = monthlyProjections.minOf { it.second }.toFloat()
                val range = maxValue - minValue

                // Linea di tendenza
                val path = Path()
                monthlyProjections.forEachIndexed { index, (_, value) ->
                    val x = (index.toFloat() / 11) * size.width
                    val y = size.height - ((value.toFloat() - minValue) / range) * size.height * 0.8f - size.height * 0.1f

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Area sotto la curva
                val areaPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3498DB).copy(alpha = 0.3f),
                            Color(0xFF3498DB).copy(alpha = 0.1f)
                        )
                    )
                )

                // Linea principale
                drawPath(
                    path = path,
                    color = Color(0xFF3498DB),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Punti
                monthlyProjections.forEachIndexed { index, (month, value) ->
                    val x = (index.toFloat() / 11) * size.width
                    val y = size.height - ((value.toFloat() - minValue) / range) * size.height * 0.8f - size.height * 0.1f

                    drawCircle(
                        color = if (month <= currentMonth) Color(0xFF3498DB) else Color(0xFFF39C12),
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF3498DB), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Attuale",
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFF39C12), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Proiezione",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Costo totale previsto: €${formatCurrency(monthlyProjections.sumOf { it.second })}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}