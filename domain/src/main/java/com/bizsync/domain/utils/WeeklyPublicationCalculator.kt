package com.bizsync.domain.utils

import java.time.DayOfWeek
import java.time.LocalDate

object WeeklyPublicationCalculator {



    /**
     * Calcola per quale settimana è possibile fare la pubblicazione
     * Logica: La finestra va da sabato a venerdì, pubblica per la settimana che inizia
     * il lunedì DOPO la fine della finestra
     *
     * Esempio:
     * - Mercoledì 9 luglio → finestra 5-11 luglio → pubblica per settimana 14-20 luglio
     * - Sabato 12 luglio → finestra 12-18 luglio → pubblica per settimana 21-27 luglio
     */
    fun getPublishableWeekStart(): LocalDate? {
        val today = LocalDate.now()

        // 1. Trova la finestra di pubblicazione corrente (sabato-venerdì)
        val currentPublicationWindow = getCurrentPublicationWindow(today)

        // 2. La settimana pubblicabile inizia il lunedì DOPO la fine della finestra
        val weekStart = currentPublicationWindow.second.plusDays(3) // venerdì + 3 = lunedì

        return weekStart
    }


    /**
     * Restituisce il lunedì di inizio della “settimana di riferimento”.
     *
     * - Da lunedì a venerdì: il prossimo lunedì.
     * - Sabato/domencia: il lunedì della settimana successiva al prossimo.
     *
     * Esempi:
     *  - mercoledì 23/07 -> lunedì 28/07
     *  - sabato 26/07    -> lunedì 05/08
     *  - domenica 27/07  -> lunedì 05/08
     */
    fun getReferenceWeekStart(date: LocalDate): LocalDate {
        // Calcola quanti giorni servono a raggiungere il prossimo lunedì
        val todayDow = date.dayOfWeek.value  // lun=1, dom=7
        val mondayValue = DayOfWeek.MONDAY.value  // 1

        // distanza a lunedì (0 = stesso giorno, 1 = next Monday if domenica, ecc.)
        var daysToNextMonday = (mondayValue - todayDow + 7) % 7
        // Se oggi è già lunedì, vogliamo il LUNEDÌ successivo, non oggi
        if (daysToNextMonday == 0) daysToNextMonday = 7

        // Se oggi è sabato(6) o domenica(7), spostiamo di un'altra settimana
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            daysToNextMonday += 7
        }

        return date.plusDays(daysToNextMonday.toLong())
    }

    /**
     * Trova la finestra di pubblicazione corrente per una data
     * Ritorna Pair(sabato_inizio, venerdì_fine)
     */
    fun getCurrentPublicationWindow(date: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = date.dayOfWeek

        return when (dayOfWeek) {
            DayOfWeek.SATURDAY -> {
                // Se oggi è sabato, la finestra inizia oggi
                val saturday = date
                val friday = saturday.plusDays(6)
                Pair(saturday, friday)
            }
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY -> {
                // Da domenica a venerdì, trova il sabato precedente
                val daysFromSaturday = when (dayOfWeek) {
                    DayOfWeek.SUNDAY -> 1
                    DayOfWeek.MONDAY -> 2
                    DayOfWeek.TUESDAY -> 3
                    DayOfWeek.WEDNESDAY -> 4
                    DayOfWeek.THURSDAY -> 5
                    DayOfWeek.FRIDAY -> 6
                    else -> 0
                }
                val saturday = date.minusDays(daysFromSaturday.toLong())
                val friday = saturday.plusDays(6)
                Pair(saturday, friday)
            }
        }
    }

    /**
     * Verifica se oggi è nella finestra di pubblicazione per una specifica settimana
     */
    fun canPublishForWeek(weekStart: LocalDate): Boolean {
        val publishableWeek = getPublishableWeekStart()
        return publishableWeek == weekStart
    }

    /**
     * Calcola la finestra di pubblicazione per una settimana specifica
     * Data una settimana (es. 14-20 luglio), ritorna la finestra di pubblicazione (5-11 luglio)
     */
    fun getPublicationWindow(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        // La finestra termina 2 giorni prima dell'inizio della settimana (venerdì)
        val endPublication = weekStart.minusDays(3) // lunedì - 3 = venerdì precedente
        // La finestra inizia 6 giorni prima della fine (sabato)
        val startPublication = endPublication.minusDays(6) // venerdì - 6 = sabato precedente
        return Pair(startPublication, endPublication)
    }

    /**
     * Verifica se oggi è nell'intervallo di pubblicazione
     */
    fun isInPublicationWindow(): Boolean {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek

        // La finestra va da sabato a venerdì
        return dayOfWeek == DayOfWeek.SATURDAY ||
                dayOfWeek == DayOfWeek.SUNDAY ||
                dayOfWeek == DayOfWeek.MONDAY ||
                dayOfWeek == DayOfWeek.TUESDAY ||
                dayOfWeek == DayOfWeek.WEDNESDAY ||
                dayOfWeek == DayOfWeek.THURSDAY ||
                dayOfWeek == DayOfWeek.FRIDAY
    }

    /**
     * Debug: Mostra informazioni sulla finestra corrente
     */
    fun getDebugInfo(): String {
        val today = LocalDate.now()
        val window = getCurrentPublicationWindow(today)
        val publishableWeek = getPublishableWeekStart()

        return """
            Oggi: $today
            Finestra corrente: ${window.first} → ${window.second}
            Settimana pubblicabile: $publishableWeek → ${publishableWeek?.plusDays(6)}
        """.trimIndent()
    }
}