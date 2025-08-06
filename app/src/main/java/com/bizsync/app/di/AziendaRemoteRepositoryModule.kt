package com.bizsync.app.di

import com.bizsync.backend.repository.AziendaRemoteRepositoryImpl
import com.bizsync.domain.repository.AziendaRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AziendaRemoteRepositoryModule {

    @Binds
    abstract fun bindAziendaRepository(
        impl: AziendaRemoteRepositoryImpl
    ): AziendaRemoteRepository
}