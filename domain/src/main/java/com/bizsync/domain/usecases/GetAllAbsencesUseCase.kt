package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import com.bizsync.domain.repository.AbsenceRemoteRepository
import javax.inject.Inject

class GetAllAbsencesUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository
) {
    suspend operator fun invoke(idUser: String, idAzienda : String): Resource<List<Absence>> {
        return try {
            absenceRemoteRepository.getAllAbsences(idUser, idAzienda)
        } catch (e: Exception) {
            Resource.Error("Errore nel recupero assenze: ${e.message}")
        }
    }
}
