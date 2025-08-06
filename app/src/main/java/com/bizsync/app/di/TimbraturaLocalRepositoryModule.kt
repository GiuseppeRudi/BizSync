package com.bizsync.app.di

import com.bizsync.cache.repository.TimbraturaLocalRepositoryImpl
import com.bizsync.domain.repository.TimbraturaLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimbraturaLocalRepositoryModule {

    @Binds
    abstract fun bindTimbraturaLocalRepository(
        impl: TimbraturaLocalRepositoryImpl
    ): TimbraturaLocalRepository
}
