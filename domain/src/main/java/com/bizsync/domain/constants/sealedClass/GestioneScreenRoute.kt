package com.bizsync.domain.constants.sealedClass// In GestioneScreenRoute.kt

sealed class GestioneScreenRoute(val route: String) {
    object Main : GestioneScreenRoute("gestione_main")

    // Manager Routes
    object Employees : GestioneScreenRoute("gestione_employees")
    object Request : GestioneScreenRoute("gestione_request")
    object Company : GestioneScreenRoute("gestione_company")
    object Reports : GestioneScreenRoute("gestione_reports")
    object Timbratura : GestioneScreenRoute("gestione_settings")
    object Logout : GestioneScreenRoute("gestione_security")

    // Employee Routes
    object Shifts : GestioneScreenRoute("gestione_shifts")
    object Absences : GestioneScreenRoute("gestione_absences")
    object EmployeeSettings : GestioneScreenRoute("gestione_employee_settings")
    object CompanyInfo : GestioneScreenRoute("gestione_company_info")
}