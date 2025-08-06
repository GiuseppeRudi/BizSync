package com.bizsync.app.di

import com.bizsync.backend.repository.UserRemoteRepositoryImpl
import com.bizsync.domain.repository.UserRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRemoteRepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        impl: UserRemoteRepositoryImpl
    ): UserRemoteRepository
}