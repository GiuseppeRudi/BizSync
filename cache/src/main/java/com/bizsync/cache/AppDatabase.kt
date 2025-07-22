package com.bizsync.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.entity.AbsenceEntity
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.cache.entity.TimbraturaEntity
import com.bizsync.cache.entity.UserEntity
import com.bizsync.cache.utils.DateConverter
import com.bizsync.cache.utils.EsitoTurnoConverter
import com.bizsync.cache.utils.ListStringConverter

@Database(
    entities = [UserEntity::class, ContrattoEntity::class, TurnoEntity::class, AbsenceEntity::class,TimbraturaEntity::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListStringConverter::class, EsitoTurnoConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contrattoDao(): ContrattoDao
    abstract fun turnoDao(): TurnoDao

    abstract fun absenceDao(): AbsenceDao

    abstract fun timbraturaDao(): TimbraturaDao

}
