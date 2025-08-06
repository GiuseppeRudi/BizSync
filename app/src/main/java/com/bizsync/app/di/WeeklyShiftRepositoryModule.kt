package com.bizsync.app.di

import com.bizsync.backend.repository.WeeklyShiftRepositoryImpl
import com.bizsync.domain.repository.WeeklyShiftRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeeklyShiftRepositoryModule {

    @Binds
    abstract fun bindWeeklyShiftRepository(
        impl: WeeklyShiftRepositoryImpl
    ): WeeklyShiftRepository
}
