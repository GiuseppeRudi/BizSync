package com.bizsync.domain.usecases

import android.util.Log
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaLocalRepository
import com.bizsync.domain.repository.TimbraturaRemoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimbratureByDateUseCase @Inject constructor(
    private val timbraturaRemoteRepository: TimbraturaRemoteRepository // Cambiato da Local a Remote
) {
    suspend operator fun invoke(startDate: String, endDate: String,userId : String): Flow<List<Timbratura>> {
        Log.d("GetTimbratureByDateUseCase", "UseCase chiamato con startDate: $startDate, endDate: $endDate")
        return timbraturaRemoteRepository.getTimbratureByDate(startDate, endDate,userId)
    }
}