package com.bizsync.app.di

import com.bizsync.backend.repository.TurnoRemoteRepositoryImpl
import com.bizsync.domain.repository.TurnoRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TurnoRemoteRepositoryModule {

    @Binds
    abstract fun bindTurnoRemoteRepository(
        impl: TurnoRemoteRepositoryImpl
    ): TurnoRemoteRepository
}