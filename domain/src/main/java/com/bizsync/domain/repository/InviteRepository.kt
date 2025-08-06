package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Invito

interface InviteRepository {
    suspend fun getInvitesByAzienda(idAzienda: String): Resource<List<Invito>>
    suspend fun caricaInvito(invito: Invito): Resource<Unit>

    suspend fun getInvitesByEmail(email: String): Resource<List<Invito>>
    suspend fun updateInvito(invito: Invito): Resource<Unit>

}