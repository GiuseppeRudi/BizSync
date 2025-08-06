package com.bizsync.app.di



import com.bizsync.backend.repository.OnBoardingPianificaRepositoryImpl
import com.bizsync.domain.repository.OnBoardingPianificaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OnBoardingRepositoryModule {

    @Binds
    abstract fun bindOnBoardingRepository(
        impl: OnBoardingPianificaRepositoryImpl
    ): OnBoardingPianificaRepository
}