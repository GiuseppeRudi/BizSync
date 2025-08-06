package com.bizsync.app.di

import com.bizsync.domain.repository.AbsenceSyncRepository
import com.bizsync.sync.orchestrator.AbsenceOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AbsenceSyncRepositoryModule {

    @Binds
    abstract fun bindAbsenceSyncRepository(
        impl: AbsenceOrchestrator
    ): AbsenceSyncRepository
}