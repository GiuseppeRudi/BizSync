package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.repository.WeeklyShiftRepository
import javax.inject.Inject

class CreateWeeklyShiftUseCase @Inject constructor(
    private val weeklyShiftRemoteRepository: WeeklyShiftRepository
) {
    suspend operator fun invoke(weeklyShift: WeeklyShift): Resource<String> {
        return weeklyShiftRemoteRepository.createWeeklyShift(weeklyShift)
    }
}
