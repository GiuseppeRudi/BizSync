package com.bizsync.app.di


import com.bizsync.domain.repository.TurnoSyncRepository
import com.bizsync.sync.orchestrator.TurnoOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TurnoSyncRepositoryModule {

    @Binds
    abstract fun bindTurnoSyncRepository(
        impl: TurnoOrchestrator
    ): TurnoSyncRepository
}
