package com.bizsync.cache.hilt

import android.content.Context
import androidx.room.Room
import com.bizsync.cache.AppDatabase
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bizsync_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideContrattoDao(db: AppDatabase): ContrattoDao = db.contrattoDao()

    @Provides
    fun provideTurnoDao(db: AppDatabase): TurnoDao = db.turnoDao()
}