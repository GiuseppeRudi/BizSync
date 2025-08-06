package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceLocalRepository
import com.bizsync.domain.repository.AbsenceSyncRepository
import javax.inject.Inject


class GetLocalAbsenceUseCase @Inject constructor(
    private val absenceLocalRepository: AbsenceLocalRepository
) {
    suspend operator fun invoke(
    ): List<Absence> {
        return absenceLocalRepository.getAbsences()
    }
}