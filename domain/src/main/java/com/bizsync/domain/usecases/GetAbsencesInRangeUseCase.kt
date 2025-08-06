package com.bizsync.domain.usecases


import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceLocalRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetAbsencesInRangeUseCase @Inject constructor(
    private val absenceLocalRepository: AbsenceLocalRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Absence>> {
        return absenceLocalRepository.getAbsencesInRange(startDate, endDate)
    }
}