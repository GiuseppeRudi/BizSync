package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.repository.WeeklyShiftRepository
import javax.inject.Inject

class UpdateWeeklyShiftStatusUseCase @Inject constructor(
    private val weeklyShiftRemoteRepository: WeeklyShiftRepository
) {
    suspend operator fun invoke(weeklyShift: WeeklyShift): Resource<Unit> {
        return weeklyShiftRemoteRepository.updateWeeklyShiftStatus(weeklyShift)
    }
}