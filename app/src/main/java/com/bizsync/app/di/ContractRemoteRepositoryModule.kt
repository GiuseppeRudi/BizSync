package com.bizsync.app.di

import com.bizsync.backend.repository.ContractRemoteRepositoryImpl
import com.bizsync.domain.repository.ContractRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContractRemoteRepositoryModule {

    @Binds
    abstract fun bindContractRemoteRepository(
        impl: ContractRemoteRepositoryImpl
    ): ContractRemoteRepository
}
