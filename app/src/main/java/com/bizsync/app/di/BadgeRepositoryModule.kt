package com.bizsync.app.di

import com.bizsync.domain.repository.BadgeRepository
import com.bizsync.sync.orchestrator.BadgeOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BadgeRepositoryModule {

    @Binds
    abstract fun bindBadgeRepository(
        impl: BadgeOrchestrator
    ): BadgeRepository
}