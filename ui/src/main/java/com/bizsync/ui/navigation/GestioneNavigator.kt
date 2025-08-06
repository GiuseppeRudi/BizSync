package com.bizsync.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.domain.constants.sealedClass.GestioneScreenRoute
import com.bizsync.ui.screens.AbsencesManagementScreen
import com.bizsync.ui.screens.CompanyInfoScreen
import com.bizsync.ui.screens.CompanyManagementScreen
import com.bizsync.ui.screens.EmployeeManagementScreen
import com.bizsync.ui.screens.EmployeeSettingsScreen
import com.bizsync.ui.screens.LogoutManagementScreen
import com.bizsync.ui.screens.MainManagementScreen
import com.bizsync.ui.screens.ReportsManagementScreen
import com.bizsync.ui.screens.RequestManagementScreen
import com.bizsync.ui.screens.ShiftsManagementScreen
import com.bizsync.ui.screens.TimbratureManagementScreen

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
        composable(GestioneScreenRoute.Main.route) {
            MainManagementScreen(
                // Manager navigation functions
                onNavigateToEmployees = {
                    navController.navigate(GestioneScreenRoute.Employees.route)
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
                    navController.navigate(GestioneScreenRoute.Timbratura.route)
                },
                onNavigateToLogout = {
                    navController.navigate(GestioneScreenRoute.Logout.route)
                },
                // Employee navigation functions
                onNavigateToShifts = {
                    navController.navigate(GestioneScreenRoute.Shifts.route)
                },
                onNavigateToAbsences = {
                    navController.navigate(GestioneScreenRoute.Absences.route)
                },
                onNavigateToEmployeeSettings = {
                    navController.navigate(GestioneScreenRoute.EmployeeSettings.route)
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

            ReportsManagementScreen(onBackClick = { navController.popBackStack() })
        }

        composable(GestioneScreenRoute.Timbratura.route) {
            TimbratureManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(GestioneScreenRoute.Logout.route) {
            LogoutManagementScreen(onLogout = onLogout)
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

        composable(GestioneScreenRoute.EmployeeSettings.route) {
            EmployeeSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

            composable(GestioneScreenRoute.CompanyInfo.route) {
                CompanyInfoScreen(onBackClick = { navController.popBackStack() })
        }

    }
}