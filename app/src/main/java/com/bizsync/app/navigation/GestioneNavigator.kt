package com.bizsync.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.screens.*
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.GestioneScreenRoute
import com.bizsync.domain.model.Absence
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.model.User
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

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

            // 1. Utenti
            val manager = User(
                uid               = "u_mgr_01",
                email             = "mario.manager@azienda.com",
                nome              = "Mario",
                cognome           = "Rossi",
                photourl          = "",
                idAzienda         = "AZ123",
                manager           = true,
                posizioneLavorativa = "Responsabile",
                dipartimento      = "DEP1"
            )
            val user1 = User(
                uid               = "u_emp_01",
                email             = "luca.bianchi@azienda.com",
                nome              = "Luca",
                cognome           = "Bianchi",
                photourl          = "",
                idAzienda         = "AZ123",
                manager           = false,
                posizioneLavorativa = "Operaio",
                dipartimento      = "DEP1"
            )
            val user2 = User(
                uid = "u_emp_02",
                email = "anna.verdi@azienda.com",
                nome = "Anna",
                cognome = "Verdi",
                photourl = "",
                idAzienda = "AZ123",
                manager = false,
                posizioneLavorativa = "Amministrativo",
                dipartimento = "DEP2"
            )
            val users = listOf(manager, user1, user2)

// 2. Contratti
            val contratto1 = Contratto(
                id                 = "C1",
                idDipendente       = user1.uid,
                idAzienda          = user1.idAzienda,
                emailDipendente    = user1.email,
                posizioneLavorativa= user1.posizioneLavorativa,
                dipartimento       = user1.dipartimento,
                tipoContratto      = "Tempo pieno",
                oreSettimanali     = "40",
                settoreAziendale   = "Produzione",
                dataInizio         = "2025-01-01",
                ccnlInfo           = Ccnlnfo(
                    settore             = "Metalmeccanico",
                    ruolo               = "Operaio",
                    ferieAnnue          = 26,
                    rolAnnui            = 20,
                    stipendioAnnualeLordo = 30000,
                    malattiaRetribuita  = 10
                ),
                ferieUsate         = 5,
                rolUsate           = 4,
                malattiaUsata      = 2
            )
            val contratto2 = Contratto(
                id = "C2",
                idDipendente = user2.uid,
                idAzienda = user2.idAzienda,
                emailDipendente = user2.email,
                posizioneLavorativa = user2.posizioneLavorativa,
                dipartimento = user2.dipartimento,
                tipoContratto = "Part‑time",
                oreSettimanali = "24",
                settoreAziendale = "Amministrazione",
                dataInizio = "2024-07-01",
                ccnlInfo = Ccnlnfo(
                    settore = "Commercio",
                    ruolo = "Impiegato",
                    ferieAnnue = 20,
                    rolAnnui = 16,
                    stipendioAnnualeLordo = 24000,
                    malattiaRetribuita = 8
                ),
                ferieUsate = 3,
                rolUsate = 2,
                malattiaUsata = 1
            )
            val contratti = listOf(contratto1, contratto2)

// 3. Assenze
            val absence1 = Absence(
                id               = "A1",
                idUser           = user1.uid,
                submittedName    = "${user1.nome} ${user1.cognome}",
                idAzienda        = user1.idAzienda,
                type             = AbsenceType.VACATION,
                startDate        = LocalDate.of(2025,7,14),
                endDate          = LocalDate.of(2025,7,16),
                startTime        = null,
                endTime          = null,
                reason           = "Vacanze estive",
                status           = AbsenceStatus.APPROVED,
                submittedDate    = LocalDate.of(2025,6,30),
                approvedBy       = manager.uid,
                approvedDate     = LocalDate.of(2025,7,1),
                comments         = "Buone vacanze",
                totalDays        = 3,
                totalHours       = null
            )
            val absence2 = Absence(
                id = "A2",
                idUser = user2.uid,
                submittedName = "${user2.nome} ${user2.cognome}",
                idAzienda = user2.idAzienda,
                type = AbsenceType.SICK_LEAVE,
                startDate = LocalDate.of(2025, 7, 15),
                endDate = LocalDate.of(2025, 7, 15),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(13, 0),
                reason = "Visita medica",
                status = AbsenceStatus.APPROVED,
                submittedDate = LocalDate.of(2025, 7, 14),
                approvedBy = manager.uid,
                approvedDate = LocalDate.of(2025, 7, 14),
                comments = null,
                totalDays = null,
                totalHours = 4
            )
            val absences = listOf(absence1, absence2)

// 4. Turni
            val turno1 = Turno(
                id                 = "T1",
                titolo             = "Turno Mattina",
                idAzienda          = manager.idAzienda,
                idDipendenti       = listOf(user1.uid),
                dipartimentoId     = user1.dipartimento,
                idFirebase         = "",
                data               = LocalDate.of(2025,7,17),
                orarioInizio       = LocalTime.of(8,0),
                orarioFine         = LocalTime.of(12,0),
                note               = emptyList(),
                pause              = emptyList(),
                createdAt          = LocalDate.of(2025,7,1),
                updatedAt          = LocalDate.of(2025,7,10)
            )
            val turno2 = Turno(
                id = "T2",
                titolo = "Turno Pomeriggio",
                idAzienda = manager.idAzienda,
                idDipendenti = listOf(user2.uid),
                dipartimentoId = user2.dipartimento,
                idFirebase = "",
                data = LocalDate.of(2025, 7, 17),
                orarioInizio = LocalTime.of(13, 0),
                orarioFine = LocalTime.of(17, 0),
                note = emptyList(),
                pause = emptyList(),
                createdAt = LocalDate.of(2025, 7, 1),
                updatedAt = LocalDate.of(2025, 7, 10)
            )
            val turni = listOf(turno1, turno2)

            ReportsManagementScreen(
                contratti = contratti,
                users     = users,
                absences  = absences,
                turni     = turni,
                onBackClick = { /* navigazione indietro */ }
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

        // all’interno della tua NavHost…
        composable(GestioneScreenRoute.CompanyInfo.route) {
            // --- 1. dummy data di esempio ---
            val allUsers = listOf(
                User("u1", "luca@azienda.com", "Luca", "Bianchi", idAzienda = "AZ1", manager = false, posizioneLavorativa = "Operaio", dipartimento = "DEP1"),
                User("u2", "mario@azienda.com", "Mario", "Rossi", idAzienda = "AZ1", manager = true, posizioneLavorativa = "Manager", dipartimento = "DEP1")
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
                    posizioneLavorativa = "Manager",
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
            CompanyInfoScreen(
                onBackClick = { navController.popBackStack() },
                user       = user,
                azienda    = azienda,
                contratto  = contratto
            )
        }

    }
}