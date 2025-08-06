package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.WeeklyShiftRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyShiftPublicationUseCase @Inject constructor(
    private val weeklyShiftRepository: WeeklyShiftRepository
) {
    suspend operator fun invoke(idAzienda: String, weekStart: LocalDate): Resource<Any?> {
        return weeklyShiftRepository.getThisWeekPublishedShift(idAzienda, weekStart)
    }
}