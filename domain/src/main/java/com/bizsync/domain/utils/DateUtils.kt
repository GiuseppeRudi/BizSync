package com.bizsync.domain.utils

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date


object DateUtils {

    // Formatter per display UI - può essere localizzato
    private val UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Formatter ISO per consistenza
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    // METODO RACCOMANDATO: Salva sempre in UTC su Firebase
    fun LocalDate.toFirebaseTimestamp(): Timestamp {
        // Converti a UTC per evitare problemi di timezone
        val instant = this.atStartOfDay(ZoneOffset.UTC).toInstant()
        return Timestamp(Date.from(instant))
    }

    // METODO RACCOMANDATO: Leggi da Firebase e converti in LocalDate
    fun Timestamp.toLocalDate(): LocalDate {
        // Converti da UTC a LocalDate (senza timezone)
        return this.toDate().toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }

    // Per display nell'UI con formato localizzato
    fun LocalDate.toUiString(): String {
        return this.format(UI_DATE_FORMATTER)
    }

    // Per parsing da input utente
    fun String.parseUiDate(): LocalDate? {
        return try {
            LocalDate.parse(this, UI_DATE_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    // ALTERNATIVA: Se hai bisogno di timestamp con ora specifica
    fun LocalDateTime.toFirebaseTimestamp(): Timestamp {
        val instant = this.atZone(ZoneOffset.UTC).toInstant()
        return Timestamp(Date.from(instant))
    }

    fun Timestamp.toLocalDateTime(): LocalDateTime {
        return this.toDate().toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
    }

    // Utility per oggi in UTC (per confronti)
    fun todayUTC(): LocalDate {
        return LocalDate.now(ZoneOffset.UTC)
    }

    // Utility per creare date per test
    fun createDate(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month, day)
    }
}
