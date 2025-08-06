package com.bizsync.app.di

import com.bizsync.cache.repository.AbsenceLocalRepositoryImpl
import com.bizsync.domain.repository.AbsenceLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AbsenceLocalRepositoryModule {

    @Binds
    abstract fun bindAbsenceLocalRepository(
        impl: AbsenceLocalRepositoryImpl
    ): AbsenceLocalRepository
}