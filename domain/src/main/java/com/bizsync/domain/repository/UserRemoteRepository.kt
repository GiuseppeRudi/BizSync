package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.bizsync.domain.model.Invito
import com.bizsync.domain.model.User

interface UserRemoteRepository {
    suspend fun addUser(user: User, uid: String): Boolean
    suspend fun updateUser(user: User, uid: String): Boolean

    suspend fun aggiornaAzienda(idAzienda: String, idUtente: String, ruolo: RuoliAzienda): Resource<Unit>
    suspend fun getUserById(userId: String): Resource<User>
    suspend fun updateAcceptInvite(invite: Invito, uid: String): Resource<Unit>
}
