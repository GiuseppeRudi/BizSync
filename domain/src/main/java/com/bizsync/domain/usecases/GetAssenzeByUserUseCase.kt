package com.bizsync.domain.usecases


import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceLocalRepository
import javax.inject.Inject

class GetAssenzeByUserUseCase @Inject constructor(
    private val absenceLocalRepository: AbsenceLocalRepository
) {
    suspend operator fun invoke(userId: String): List<Absence> {
        return absenceLocalRepository.getAbsencesByUser(userId)
    }
}