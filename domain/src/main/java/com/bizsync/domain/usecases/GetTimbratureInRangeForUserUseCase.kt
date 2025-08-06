package com.bizsync.domain.usecases

import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaLocalRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTimbratureInRangeForUserUseCase @Inject constructor(
    private val timbraturaLocalRepository: TimbraturaLocalRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate, userId: String): Flow<List<Timbratura>> {
        return timbraturaLocalRepository.getTimbratureInRangeForUser(startDate, endDate, userId)
    }
}