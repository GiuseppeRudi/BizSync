package com.bizsync.domain.usecases

import android.util.Log
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.RequestDecisionResult
import com.bizsync.domain.repository.AbsenceRemoteRepository
import com.bizsync.domain.repository.ContractSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class HandleRequestDecisionUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository,
    private val contractSyncRepository: ContractSyncRepository
) {
    suspend operator fun invoke(
        approver: String,
        request: Absence,
        isApproved: Boolean,
        comment: String,
        employeeContract: Contratto?
    ): Resource<RequestDecisionResult> {
        return try {
            // ✅ Step 1: Crea richiesta aggiornata
            val updatedRequest = request.copy(
                status = if (isApproved) AbsenceStatus.APPROVED else AbsenceStatus.REJECTED,
                comments = comment,
                approvedBy = approver,
                approvedDate = LocalDate.now()
            )

            // ✅ Step 2: Aggiorna la richiesta di assenza
            when (val absenceResult = absenceRemoteRepository.updateAbsence(updatedRequest)) {
                is Resource.Success -> {
                    var updatedContract: Contratto? = null
                    var contractUpdateSuccess = true
                    var contractError: String? = null

                    // ✅ Step 3: Se approvata e ha contratto, aggiorna il contratto
                    if (isApproved && employeeContract != null) {
                        val newContract = updateContractForApprovedAbsence(employeeContract, updatedRequest)

                        if (newContract != employeeContract) {
                            when (val contractResult = contractSyncRepository.updateContratto(newContract)) {
                                is Resource.Success -> {
                                    updatedContract = newContract
                                    Log.d("REQUEST_DECISION", "✅ Contratto aggiornato e sincronizzato")
                                }
                                is Resource.Error -> {
                                    contractUpdateSuccess = false
                                    contractError = contractResult.message
                                    Log.e("REQUEST_DECISION", "❌ Errore aggiornamento contratto: ${contractResult.message}")
                                }
                                is Resource.Empty -> {
                                    contractUpdateSuccess = false
                                    contractError = "Risposta vuota dall'aggiornamento contratto"
                                }
                            }
                        }
                    }

                    Resource.Success(
                        RequestDecisionResult(
                            updatedRequest = updatedRequest,
                            updatedContract = updatedContract,
                            contractUpdateSuccess = contractUpdateSuccess,
                            contractError = contractError,
                            isApproved = isApproved
                        )
                    )
                }
                is Resource.Error -> {
                    Resource.Error(absenceResult.message ?: "Errore durante l'aggiornamento della richiesta")
                }
                is Resource.Empty -> {
                    Resource.Error("Risposta vuota dall'aggiornamento richiesta")
                }
            }
        } catch (e: Exception) {
            Resource.Error("Errore imprevisto: ${e.message}")
        }
    }

    // ✅ Business logic helper nel Use Case
    private fun updateContractForApprovedAbsence(
        contract: Contratto,
        approvedAbsence: Absence
    ): Contratto {
        return when (approvedAbsence.type) {
            AbsenceType.VACATION -> {
                val daysToAdd = approvedAbsence.totalDays ?: 0
                contract.copy(ferieUsate = contract.ferieUsate + daysToAdd)
            }

            AbsenceType.ROL -> {
                val hoursToAdd = approvedAbsence.totalHours ?: 0
                contract.copy(rolUsate = contract.rolUsate + hoursToAdd)
            }

            AbsenceType.SICK_LEAVE -> {
                val daysToAdd = approvedAbsence.totalDays ?: 0
                contract.copy(malattiaUsata = contract.malattiaUsata + daysToAdd)
            }

            AbsenceType.PERSONAL_LEAVE,
            AbsenceType.UNPAID_LEAVE,
            AbsenceType.STRIKE -> {
                contract // ✅ Nessun aggiornamento necessario
            }
        }
    }
}