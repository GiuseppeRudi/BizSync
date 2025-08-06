package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito
import com.bizsync.domain.repository.InviteRepository
import javax.inject.Inject

class LoadInvitesUseCase @Inject constructor(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(idAzienda: String): Resource<List<Invito>> {
        return try {
            inviteRepository.getInvitesByAzienda(idAzienda)
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento inviti: ${e.message}")
        }
    }
}