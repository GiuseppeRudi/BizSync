package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceSyncRepository
import javax.inject.Inject

class GetAbsencesUseCase @Inject constructor(
    private val absenceRepository: AbsenceSyncRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        idEmployee: String? = null,
        forceRefresh: Boolean = false
    ): Resource<List<Absence>> {
        return absenceRepository.getAbsences(idAzienda, idEmployee, forceRefresh)
    }
}