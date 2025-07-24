package com.bizsync.backend.prompts


import com.bizsync.domain.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TurniPrompts {

    fun getGeneraTurniPrompt(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        dipendentiDisponibili: DipendentiGiorno,
        statoSettimanale: Map<String, StatoSettimanaleDipendente>,
        turniEsistenti: List<Turno>,
        descrizioneAggiuntiva: String = ""
    ): String {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Prepara info dipendenti disponibili
        val dipendentiInfo = dipendentiDisponibili.utenti.mapNotNull { user ->
            val stato = dipendentiDisponibili.statoPerUtente[user.uid] ?: return@mapNotNull null
            val statoSettimanaleUser = statoSettimanale[user.uid]

            if (stato.isAssenteTotale) return@mapNotNull null

            val oreRimanenti = statoSettimanaleUser?.let {
                it.oreContrattoSettimana - it.oreAssegnateSettimana
            } ?: 40

            """
            {
                "id": "${user.uid}",
                "nome": "${user.nome} ${user.cognome}",
                "oreRimanentiSettimana": $oreRimanenti,
                "assenzaParziale": ${stato.assenzaParziale?.let {
                """{ "inizio": "${it.inizio}", "fine": "${it.fine}" }"""
            } ?: "null"}
            }
            """.trimIndent()
        }.joinToString(",\n")

        // Prepara info turni esistenti
        val turniEsistentiInfo = turniEsistenti.joinToString(",\n") { turno ->
            """
            {
                "orarioInizio": "${turno.orarioInizio.format(timeFormatter)}",
                "orarioFine": "${turno.orarioFine.format(timeFormatter)}",
                "dipendenti": [${turno.idDipendenti.joinToString(", ") { "\"$it\"" }}]
            }
            """.trimIndent()
        }

        return """
            Sei un esperto pianificatore di turni lavorativi. Devi generare turni ottimali per coprire l'orario lavorativo.

            DATI DEL DIPARTIMENTO:
            - Nome: ${dipartimento.nomeArea}
            - Orario apertura: ${dipartimento.orariSettimanali.get(giornoSelezionato.dayOfWeek)?.first?.format(timeFormatter)}
            - Orario chiusura: ${dipartimento.orariSettimanali.get(giornoSelezionato.dayOfWeek)?.second?.format(timeFormatter)}
            - Giorno: ${giornoSelezionato.dayOfWeek} ${giornoSelezionato}

            DIPENDENTI DISPONIBILI:
            [
            $dipendentiInfo
            ]

            TURNI GIÀ ESISTENTI (da considerare):
            [
            $turniEsistentiInfo
            ]

            ${if (descrizioneAggiuntiva.isNotEmpty()) "INDICAZIONI AGGIUNTIVE: $descrizioneAggiuntiva" else ""}

            REGOLE DA RISPETTARE:
            1. Coprire tutto l'orario del dipartimento ( ${dipartimento.orariSettimanali.get(giornoSelezionato.dayOfWeek)?.first?.format(timeFormatter)} -  ${dipartimento.orariSettimanali.get(giornoSelezionato.dayOfWeek)?.second?.format(timeFormatter)})
            2. NON ASSEGNARE TURNI IN FASCIE D'ORARIO DOVE CE GIA LO STESSO DIPENDENTE CHE EFFETTUA TURNI GIA ESISTENTI
            3. SE I TURNI SECONDO ME VANNO GIA BENE NON RESTITUIRE NULLA 
            4. NON superare le ore rimanenti settimanali per ogni dipendente
            5. Rispettare eventuali assenze parziali dei dipendenti
            6. Minimizzare i buchi di copertura
            7. Evitare sovrapposizioni con turni esistenti
            8. Preferire turni di 4-8 ore
            9. Assicurare pause pranzo per turni > 6 ore
            10. Distribuire equamente il carico tra i dipendenti disponibili

            FORMATO RISPOSTA - Rispondi SOLO con un JSON valido:
            {
                "turniGenerati": [
                    {
                        "titolo": "Nome descrittivo del turno",
                        "orarioInizio": "HH:mm",
                        "orarioFine": "HH:mm",
                        "idDipendenti": ["id1", "id2"],
                        "pause": [
                            {
                                "tipo": "PAUSA_PRANZO|PAUSA_CAFFE|RIPOSO_BREVE",
                                "durataMinuti": 30,
                                "retribuita": true|false
                            }
                        ],
                        "note": "Eventuali note sul turno"
                    }
                ],
                "coperturaTotale": true|false,
                "motivoCoperturaParziale": "Solo se coperturaTotale è false"
            }

            IMPORTANTE: Genera SOLO il JSON, nessun altro testo.
        """.trimIndent()
    }
}