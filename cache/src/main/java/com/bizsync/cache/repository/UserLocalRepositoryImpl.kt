package com.bizsync.cache.repository

import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.UserEntityMapper.toDomain
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.UserLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserLocalRepository {

    override suspend fun getDipendenti(): List<User> {
        return userDao.getDipendenti().map { it.toDomain() }
    }

    override suspend fun getDipendentiFull(): List<User> {
        return userDao.getDipendentiFull().map { it.toDomain() }
    }


    override suspend fun deleteAll() {
        userDao.deleteAll()
    }

}