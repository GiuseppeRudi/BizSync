package com.bizsync.domain.usecases

import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.repository.AbsenceLocalRepository
import com.bizsync.domain.repository.TurnoLocalRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


class CheckEmployeeAvailabilityUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository,
    private val absenceLocalRepository: AbsenceLocalRepository
) {
    suspend operator fun invoke(
        employeeId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Boolean {
        // 1. Usa getTurniInRange esistente per prendere tutti i turni del giorno
        val turniDelGiorno = turnoLocalRepository.getTurniInRange(date, date)

        // Filtra solo i turni del dipendente
        val turniDipendente = turniDelGiorno.filter { turno ->
            turno.idDipendenti.contains(employeeId)
        }

        // Verifica sovrapposizioni orarie
        for (turno in turniDipendente) {
            // Controlla sovrapposizione
            val turnoStart = turno.orarioInizio
            val turnoEnd = turno.orarioFine

            // Se c'è sovrapposizione, non è disponibile
            if (!(endTime <= turnoStart || startTime >= turnoEnd)) {
                return false
            }
        }

        // 2. Usa getAbsencesInRange esistente per verificare assenze
        val absences = absenceLocalRepository.getAbsencesInRange(date, date).first()

        // Filtra assenze del dipendente e approvate
        val employeeAbsences = absences.filter { absence ->
            absence.idUser == employeeId &&
                    absence.status == AbsenceStatus.APPROVED
        }

        // Se ha assenze approvate per quel giorno, non è disponibile
        return employeeAbsences.isEmpty()
    }
}