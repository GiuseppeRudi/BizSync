package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.*
import com.bizsync.domain.constants.sealedClass.GestioneScreenRoute
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.model.User
import java.time.DayOfWeek

@Composable
fun GestioneNavigator(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GestioneScreenRoute.Main.route,
        modifier = modifier
    ) {
        // Schermata principale con tutte le navigation functions
        composable(GestioneScreenRoute.Main.route) {
            MainManagementScreen(
                // isManager navigation functions
                onNavigateToEmployees = {
                    navController.navigate(GestioneScreenRoute.Employees.route)
                },
                onNavigateToProjects = {
                    navController.navigate(GestioneScreenRoute.Projects.route)
                },
                onNavigateToFinance = {
                    navController.navigate(GestioneScreenRoute.Finance.route)
                },
                onNavigateToRequest = {
                    navController.navigate(GestioneScreenRoute.Request.route)
                },
                onNavigateToManageCompany = {
                    navController.navigate(GestioneScreenRoute.Company.route)
                },
                onNavigateToReports = {
                    navController.navigate(GestioneScreenRoute.Reports.route)
                },
                onNavigateToTimbratura = {
                    navController.navigate(GestioneScreenRoute.Settings.route)
                },
                onNavigateToLogout = {
                    navController.navigate(GestioneScreenRoute.Security.route)
                },
                // Employee navigation functions
                onNavigateToShifts = {
                    navController.navigate(GestioneScreenRoute.Shifts.route)
                },
                onNavigateToAbsences = {
                    navController.navigate(GestioneScreenRoute.Absences.route)
                },
                onNavigateToActivities = {
                    navController.navigate(GestioneScreenRoute.Activities.route)
                },
                onNavigateToEmployeeSettings = {
                    navController.navigate(GestioneScreenRoute.EmployeeSettings.route)
                },
                onNavigateToEmployeeFinance = {
                    navController.navigate(GestioneScreenRoute.EmployeeFinance.route)
                },
                onNavigateToCompanyInfo = {
                    navController.navigate(GestioneScreenRoute.CompanyInfo.route)
                }
            )
        }

        // isManager Screens
        composable(GestioneScreenRoute.Employees.route) {
            EmployeeManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Projects.route) {
            ProjectsManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Finance.route) {
            FinanceManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Request.route) {
            RequestManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Company.route) {
            CompanyManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Reports.route) {

            ReportsManagementScreen(onBackClick = { navController.popBackStack()} )
        }

        composable(GestioneScreenRoute.Settings.route) {
            TimbratureManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Security.route) {
            LogoutManagementScreen(
                onLogout = onLogout,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Employee Screens
        composable(GestioneScreenRoute.Shifts.route) {
            ShiftsManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Absences.route) {
            AbsencesManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Activities.route) {
            ActivitiesManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.EmployeeSettings.route) {
            EmployeeSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.EmployeeFinance.route) {
            EmployeeFinanceScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // all’interno della tua NavHost…
        composable(GestioneScreenRoute.CompanyInfo.route) {
            // --- 1. dummy data di esempio ---
            val allUsers = listOf(
                User("u1", "luca@azienda.com", "Luca", "Bianchi", idAzienda = "AZ1", isManager = false, posizioneLavorativa = "Operaio", dipartimento = "DEP1"),
                User("u2", "mario@azienda.com", "Mario", "Rossi", idAzienda = "AZ1", isManager = true, posizioneLavorativa = "isManager", dipartimento = "DEP1")
            )
            val allAziende = listOf(
                Azienda(
                    idAzienda = "AZ1",
                    nome = "Acme Corp",
                    areeLavoro = listOf(
                        AreaLavoro(id = "DEP1", nomeArea = "Produzione"),
                        AreaLavoro(id = "DEP2", nomeArea = "Amministrazione")
                    ),
                    turniFrequenti = listOf(
                        TurnoFrequente(nome = "Mattina", oraInizio = "08:00", oraFine = "12:00"),
                        TurnoFrequente(nome = "Pomeriggio", oraInizio = "13:00", oraFine = "17:00")
                    ),
                    numDipendentiRange = "1-50",
                    sector = "Manifatturiero",
                    giornoPubblicazioneTurni = DayOfWeek.MONDAY
                )
            )
            val allContratti = listOf(
                Contratto(
                    id = "C1",
                    idDipendente = "u1",
                    idAzienda = "AZ1",
                    emailDipendente = "luca@azienda.com",
                    posizioneLavorativa = "Operaio",
                    dipartimento = "DEP1",
                    tipoContratto = "Tempo Pieno",
                    oreSettimanali = "40",
                    settoreAziendale = "Produzione",
                    dataInizio = "2025-01-01"
                ),
                Contratto(
                    id = "C2",
                    idDipendente = "u2",
                    idAzienda = "AZ1",
                    emailDipendente = "mario@azienda.com",
                    posizioneLavorativa = "isManager",
                    dipartimento = "DEP1",
                    tipoContratto = "Tempo Pieno",
                    oreSettimanali = "40",
                    settoreAziendale = "Produzione",
                    dataInizio = "2025-01-01"
                )
            )

            // --- 2. estraggo gli oggetti da passare ---
            val user    = allUsers.first { it.uid == "u1" }
            val azienda = allAziende.first { it.idAzienda == user.idAzienda }
            val contratto = allContratti.first { it.idDipendente == user.uid && it.idAzienda == azienda.idAzienda }

            // --- 3. il composable con i parametri corretti ---
            CompanyInfoScreen(onBackClick = { navController.popBackStack() },)
        }

    }
}