package com.bizsync.domain.repository

import com.bizsync.domain.model.User

interface UserLocalRepository {
    suspend fun getDipendenti(): List<User>
    suspend fun getDipendentiFull(): List<User>
    suspend fun deleteAll()

}