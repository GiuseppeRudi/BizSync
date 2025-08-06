package com.bizsync.app.di


import com.bizsync.backend.repository.InviteRepositoryImpl
import com.bizsync.domain.repository.InviteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InviteRepositoryModule {

    @Binds
    abstract fun bindInviteRepository(
        impl: InviteRepositoryImpl
    ): InviteRepository
}