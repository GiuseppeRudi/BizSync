package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.TimbraturaRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTimbratureByAziendaUseCase @Inject constructor(
    private val timbraturaRemoteRepository: TimbraturaRemoteRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Timbratura>> {
        return timbraturaRemoteRepository.getTimbratureByAzienda(idAzienda, startDate, endDate)
    }
}