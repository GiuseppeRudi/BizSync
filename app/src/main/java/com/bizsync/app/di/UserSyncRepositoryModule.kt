package com.bizsync.app.di

import com.bizsync.domain.repository.UserSyncRepository
import com.bizsync.sync.orchestrator.UserOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSyncRepositoryModule {

    @Binds
    abstract fun bindUserSyncRepository(
        impl: UserOrchestrator
    ): UserSyncRepository
}
