package com.bizsync.app.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
        // Schermata principale (passa il controller)
        composable(GestioneScreenRoute.Main.route) {
            MainManagementScreen( onNavigateToEmployees = {
                navController.navigate(GestioneScreenRoute.Employees.route)
            },
                onNavigateToProjects = {
                    navController.navigate(GestioneScreenRoute.Projects.route)
                },
                onNavigateToFinance = {
                    navController.navigate(GestioneScreenRoute.Finance.route)
                },
                onNavigateToInventory = {
                    navController.navigate(GestioneScreenRoute.Inventory.route)
                },
                onNavigateToCustomers = {
                    navController.navigate(GestioneScreenRoute.Customers.route)
                },
                onNavigateToReports = {
                    navController.navigate(GestioneScreenRoute.Reports.route)
                },
                onNavigateToSettings = {
                    navController.navigate(GestioneScreenRoute.Settings.route)
                },
                onNavigateToSecurity = {
                    navController.navigate(GestioneScreenRoute.Security.route)
                })
        }

        // Schermate secondarie (passa lambda)
        composable(GestioneScreenRoute.Employees.route) {
            EmployeeManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

//        composable(GestioneScreenRoute.Projects.route) {
//            ProjectsManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Finance.route) {
//            FinanceManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Inventory.route) {
//            InventoryManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Customers.route) {
//            CustomersManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Reports.route) {
//            ReportsManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Settings.route) {
//            SettingsManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        composable(GestioneScreenRoute.Security.route) {
//            SecurityManagementScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }
    }
}
