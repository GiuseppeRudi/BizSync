package com.bizsync.cache.utils

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

class DateConverter {

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    // --------------------- FIREBASE Timestamp <-> Long ---------------------

    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }

    // --------------------- LocalDate <-> String ---------------------

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    // --------------------- LocalTime <-> String ---------------------

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }
}
