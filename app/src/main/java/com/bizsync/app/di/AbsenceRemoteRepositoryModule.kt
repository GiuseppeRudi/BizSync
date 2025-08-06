package com.bizsync.app.di

import com.bizsync.backend.repository.AbsenceRemoteRepositoryImpl
import com.bizsync.domain.repository.AbsenceRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AbsenceRemoteRepositoryModule {

    @Binds
    abstract fun bindAbsenceRepository(
        impl: AbsenceRemoteRepositoryImpl
    ): AbsenceRemoteRepository
}
