package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceRemoteRepository
import javax.inject.Inject

class SaveAbsenceUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository
) {
    suspend operator fun invoke(absence: Absence): Resource<String> {
        return try {
            absenceRemoteRepository.salvaAbsence(absence)
        } catch (e: Exception) {
            Resource.Error("Errore nel salvataggio assenza: ${e.message}")
        }
    }
}