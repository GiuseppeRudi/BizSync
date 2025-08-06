package com.bizsync.app.di

import com.bizsync.domain.repository.ContractSyncRepository
import com.bizsync.sync.orchestrator.ContrattoOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContractSyncRepositoryModule {

    @Binds
    abstract fun bindContractSyncRepository(
        impl: ContrattoOrchestrator
    ): ContractSyncRepository
}