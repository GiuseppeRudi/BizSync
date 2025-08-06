package com.bizsync.app.di

import com.bizsync.cache.repository.TurnoLocalRepositoryImpl
import com.bizsync.domain.repository.TurnoLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TurnoLocalRepositoryModule {

    @Binds
    abstract fun bindTurnoLocalRepository(
        impl: TurnoLocalRepositoryImpl
    ): TurnoLocalRepository
}
