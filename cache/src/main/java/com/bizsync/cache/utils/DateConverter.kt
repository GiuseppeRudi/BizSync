package com.bizsync.cache.utils

import androidx.room.TypeConverter
import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

class DateConverter {

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // --------------------- FIREBASE Timestamp <-> Long ---------------------

    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? = value?.toDate()?.time

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? = value?.let { Timestamp(Date(it)) }

    // --------------------- LocalDate <-> String ---------------------

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    // --------------------- LocalTime <-> String ---------------------

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    // --------------------- LocalDateTime <-> String ---------------------

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateTimeFormatter) }

    // --------------------- Enum: TipoTimbratura <-> String ---------------------

    @TypeConverter
    fun fromTipoTimbratura(tipo: TipoTimbratura?): String? = tipo?.name

    @TypeConverter
    fun toTipoTimbratura(value: String?): TipoTimbratura? = value?.let { TipoTimbratura.valueOf(it) }

    // --------------------- Enum: StatoTimbratura <-> String ---------------------

    @TypeConverter
    fun fromStatoTimbratura(stato: StatoTimbratura?): String? = stato?.name

    @TypeConverter
    fun toStatoTimbratura(value: String?): StatoTimbratura? = value?.let { StatoTimbratura.valueOf(it) }

    // --------------------- Enum: ZonaLavorativa <-> String ---------------------

    @TypeConverter
    fun fromZonaLavorativa(zona: ZonaLavorativa?): String? = zona?.name

    @TypeConverter
    fun toZonaLavorativa(value: String?): ZonaLavorativa? = value?.let { ZonaLavorativa.valueOf(it) }
}
