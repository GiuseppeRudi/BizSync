package com.bizsync.domain.usecases

import android.util.Log
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
    companion object {
        private const val TAG = "CheckEmpAvailability"
    }

    suspend operator fun invoke(
        employeeId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Boolean {
        Log.d(TAG, "Verifica disponibilità per dipendente: $employeeId, data: $date, orario: $startTime-$endTime")

        val turniDelGiorno = turnoLocalRepository.getTurniInRange(date, date)
        Log.d(TAG, "Turni trovati nella data $date: ${turniDelGiorno.size}")

        val turniDipendente = turniDelGiorno.filter { turno ->
            turno.idDipendenti.contains(employeeId)
        }
        Log.d(TAG, "Turni dipendente $employeeId: ${turniDipendente.size}")

        for (turno in turniDipendente) {
            val turnoStart = turno.orarioInizio
            val turnoEnd = turno.orarioFine
            Log.d(TAG, "Controllo sovrapposizione con turno ${turno.id}: $turnoStart - $turnoEnd")

            if (!(endTime <= turnoStart || startTime >= turnoEnd)) {
                Log.d(TAG, "Sovrapposizione trovata con turno ${turno.id}, dipendente NON disponibile")
                return false
            }
        }

        val absences = absenceLocalRepository.getAbsencesInRange(date, date).first()
        Log.d(TAG, "Assenze trovate nella data $date: ${absences.size}")

        val employeeAbsences = absences.filter { absence ->
            absence.idUser == employeeId &&
                    absence.status == AbsenceStatus.APPROVED
        }
        Log.d(TAG, "Assenze approvate per dipendente $employeeId: ${employeeAbsences.size}")

        val available = employeeAbsences.isEmpty()
        Log.d(TAG, "Dipendente disponibile? $available")
        return available
    }
}