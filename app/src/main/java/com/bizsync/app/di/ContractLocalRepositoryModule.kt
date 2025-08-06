package com.bizsync.app.di


import com.bizsync.cache.repository.ContractLocalRepositoryImpl
import com.bizsync.domain.repository.ContractLocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContractLocalRepositoryModule {

    @Binds
    abstract fun bindContractLocalRepository(
        impl: ContractLocalRepositoryImpl
    ): ContractLocalRepository
}
