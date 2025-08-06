package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.User

interface UserSyncRepository {
    suspend fun updateDipartimentoDipendenti(users: List<User>): Resource<Unit>
    suspend fun getDipendenti(idAzienda: String, idUser: String, forceRefresh: Boolean = false): Resource<List<User>>
}