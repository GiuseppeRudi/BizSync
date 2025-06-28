package com.bizsync.domain.constants.sealedClass// In GestioneScreenRoute.kt

sealed class GestioneScreenRoute(val route: String) {
    object Main : GestioneScreenRoute("gestione_main")

    // Manager Routes
    object Employees : GestioneScreenRoute("gestione_employees")
    object Projects : GestioneScreenRoute("gestione_projects")
    object Finance : GestioneScreenRoute("gestione_finance")
    object Inventory : GestioneScreenRoute("gestione_inventory")
    object Customers : GestioneScreenRoute("gestione_customers")
    object Reports : GestioneScreenRoute("gestione_reports")
    object Settings : GestioneScreenRoute("gestione_settings")
    object Security : GestioneScreenRoute("gestione_security")

    // Employee Routes
    object Shifts : GestioneScreenRoute("gestione_shifts")
    object Absences : GestioneScreenRoute("gestione_absences")
    object Activities : GestioneScreenRoute("gestione_activities")
    object EmployeeSettings : GestioneScreenRoute("gestione_employee_settings")
    object EmployeeFinance : GestioneScreenRoute("gestione_employee_finance")
    object CompanyInfo : GestioneScreenRoute("gestione_company_info")
}