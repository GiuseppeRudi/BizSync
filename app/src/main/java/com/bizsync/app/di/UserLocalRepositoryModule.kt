package com.bizsync.app.di

import com.bizsync.cache.repository.UserLocalRepositoryImpl
import com.bizsync.domain.repository.UserLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserLocalRepositoryModule {

    @Binds
    abstract fun bindUserLocalRepository(
        impl: UserLocalRepositoryImpl
    ): UserLocalRepository
}
