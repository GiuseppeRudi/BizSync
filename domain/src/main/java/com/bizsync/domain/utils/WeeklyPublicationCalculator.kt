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
        val daysSinceSaturday = ((dayOfWeek.value+1) % 7).toLong()
        val saturday = date.minusDays(daysSinceSaturday)
        val friday = saturday.plusDays(6)
        return Pair(saturday, friday)
    }


}