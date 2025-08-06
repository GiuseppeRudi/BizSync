package com.bizsync.domain.usecases

import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaLocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimbratureByDateUseCase @Inject constructor(
    private val timbraturaLocalRepository: TimbraturaLocalRepository
) {
    suspend operator fun invoke(startDate: String, endDate: String): Flow<List<Timbratura>> {
        return timbraturaLocalRepository.getTimbratureByDate(startDate, endDate)
    }
}