package com.bizsync.domain.usecases

import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.RequestsData
import com.bizsync.domain.repository.AbsenceRemoteRepository
import javax.inject.Inject

class FetchAllRequestsUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository
) {
    suspend operator fun invoke(idAzienda: String): Resource<RequestsData> {
        return try {
            when (val result = absenceRemoteRepository.getAllAbsencesByAzienda(idAzienda)) {
                is Resource.Success -> {
                    val allAbsences = result.data

                    // ✅ Business logic nel Use Case
                    val pending = allAbsences.filter { it.status == AbsenceStatus.PENDING }
                    val history = allAbsences.filter { it.status != AbsenceStatus.PENDING }

                    Resource.Success(
                        RequestsData(
                            pendingRequests = pending,
                            historyRequests = history,
                            totalRequests = allAbsences.size
                        )
                    )
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Errore nel caricamento delle richieste")
                }
                is Resource.Empty -> {
                    Resource.Success(
                        RequestsData(
                            pendingRequests = emptyList(),
                            historyRequests = emptyList(),
                            totalRequests = 0
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento richieste: ${e.message}")
        }
    }
}