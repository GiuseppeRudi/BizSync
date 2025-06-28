package com.bizsync.domain.constants.sealedClass



sealed class GestioneScreenRoute(val route: String) {
    object Main : GestioneScreenRoute("gestione_main")
    object Employees : GestioneScreenRoute("gestione_employees")
    object Projects : GestioneScreenRoute("gestione_projects")
    object Finance : GestioneScreenRoute("gestione_finance")
    object Inventory : GestioneScreenRoute("gestione_inventory")
    object Customers : GestioneScreenRoute("gestione_customers")
    object Reports : GestioneScreenRoute("gestione_reports")
    object Settings : GestioneScreenRoute("gestione_settings")
    object Security : GestioneScreenRoute("gestione_security")
}