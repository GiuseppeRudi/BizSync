package com.bizsync.cache.utils

import androidx.room.TypeConverter
import com.bizsync.domain.constants.enumClass.EsitoTurno


class EsitoTurnoConverter {
    @TypeConverter
    fun fromEsitoTurno(value: EsitoTurno): String = value.name

    @TypeConverter
    fun toEsitoTurno(value: String): EsitoTurno = EsitoTurno.valueOf(value)
}
