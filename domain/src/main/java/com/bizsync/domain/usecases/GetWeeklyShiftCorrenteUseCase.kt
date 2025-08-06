package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.repository.WeeklyShiftRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyShiftCorrenteUseCase @Inject constructor(
    private val weeklyShiftRemoteRepository: WeeklyShiftRepository
) {
    suspend operator fun invoke(weekStart: LocalDate): Resource<WeeklyShift?> {
        return weeklyShiftRemoteRepository.getWeeklyShiftCorrente(weekStart)
    }
}