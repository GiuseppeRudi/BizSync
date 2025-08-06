package com.bizsync.app.di

import com.bizsync.domain.repository.HashRepository
import com.bizsync.sync.repository.HashRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HashRepositoryModule {

    @Binds
    abstract fun bindHashRepository(
        impl: HashRepositoryImpl
    ): HashRepository
}
