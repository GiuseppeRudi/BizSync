package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import java.time.LocalDate
import javax.inject.Inject


class GetTurniByDateRangeUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate,
        idDipendente: String
    ): List<Turno> {
        // Usa la funzione esistente getTurniInRange
        val allTurni = turnoLocalRepository.getTurniInRange(startDate, endDate)

        // Filtra per azienda e dipendente
        return allTurni.filter { turno ->
            turno.idAzienda == idAzienda &&
                    turno.idDipendenti.contains(idDipendente)
        }
    }
}
