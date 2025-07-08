package com.bizsync.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.cache.entity.UserEntity
import com.bizsync.cache.utils.TimestampConverter

@Database(
    entities = [UserEntity::class, ContrattoEntity::class, TurnoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contrattoDao(): ContrattoDao
    abstract fun turnoDao(): TurnoDao
}
