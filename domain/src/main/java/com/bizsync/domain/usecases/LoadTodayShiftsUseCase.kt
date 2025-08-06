package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.TurnoWithUsers
import com.bizsync.domain.repository.TurnoLocalRepository
import com.bizsync.domain.repository.UserLocalRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class LoadTodayShiftsUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository,
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(): Resource<List<TurnoWithUsers>> {
        return try {
            val today = LocalDate.now()
            val todayShifts = turnoLocalRepository.getTurniByDate(today).first()
            val users = userLocalRepository.getDipendentiFull()

            // ✅ Business logic nel Use Case
            val shiftsWithUsers = todayShifts.map { turno ->
                val turnoUsers = users.filter { user ->
                    turno.idDipendenti.contains(user.uid)
                }
                TurnoWithUsers(turno, turnoUsers)
            }.sortedBy { it.turno.orarioInizio }

            Resource.Success(shiftsWithUsers)
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento turni: ${e.message}")
        }
    }
}