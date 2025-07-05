package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.*
import com.bizsync.domain.constants.sealedClass.GestioneScreenRoute

@Composable
fun GestioneNavigator(
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
                // Manager navigation functions
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
                onNavigateToSettings = {
                    navController.navigate(GestioneScreenRoute.Settings.route)
                },
                onNavigateToSecurity = {
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

        // Manager Screens
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
            ReportsManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Settings.route) {
            SettingsManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Security.route) {
            SecurityManagementScreen(
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

        composable(GestioneScreenRoute.CompanyInfo.route) {
            CompanyInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}