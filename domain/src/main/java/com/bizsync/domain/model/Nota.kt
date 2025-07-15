package com.bizsync.domain.model



import com.bizsync.domain.constants.enumClass.TipoNota
import java.time.LocalDate
import java.util.UUID

data class Nota(
    val id: String = UUID.randomUUID().toString(),
    val testo: String = "",
    val tipo: TipoNota = TipoNota.GENERALE,
    val autore: String = "", // ID dell'utente che ha creato la nota
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
) {
    /**
     * Verifica se la nota è stata modificata
     */
    fun isModificata(): Boolean {
        return updatedAt.isAfter(createdAt)
    }

    /**
     * Verifica se la nota è vuota
     */
    fun isEmpty(): Boolean {
        return testo.isBlank()
    }

    /**
     * Ottiene una versione abbreviata del testo
     */
    fun getTestoAbbreviato(maxLength: Int = 50): String {
        return if (testo.length <= maxLength) {
            testo
        } else {
            testo.take(maxLength - 3) + "..."
        }
    }

    /**
     * Verifica se la nota è di tipo importante
     */
    fun isImportante(): Boolean {
        return tipo == TipoNota.IMPORTANTE || tipo == TipoNota.SICUREZZA
    }

    /**
     * Valida se la nota è completa
     */
    fun isValid(): Boolean {
        return testo.isNotBlank() && testo.length <= 500
    }
}

// Classe per template note predefinite
data class TemplateNota(
    val testo: String,
    val tipo: TipoNota,
    val categoria: String = "Generale"
)

// Template note predefinite per l'app
object NotaTemplates {
    val templates = listOf(
        TemplateNota(
            testo = "Verificare funzionamento di tutte le attrezzature prima dell'inizio del turno",
            tipo = TipoNota.EQUIPMENT,
            categoria = "Attrezzature"
        ),
        TemplateNota(
            testo = "Rispettare rigorosamente le procedure di sicurezza",
            tipo = TipoNota.SICUREZZA,
            categoria = "Sicurezza"
        ),
        TemplateNota(
            testo = "Cliente importante in visita - prestare particolare attenzione",
            tipo = TipoNota.CLIENTE,
            categoria = "Cliente"
        ),
        TemplateNota(
            testo = "Seguire la checklist standard per le operazioni di routine",
            tipo = TipoNota.PROCEDURA,
            categoria = "Procedura"
        ),
        TemplateNota(
            testo = "Aggiornamento importante: nuove procedure operative",
            tipo = TipoNota.IMPORTANTE,
            categoria = "Comunicazioni"
        ),
        TemplateNota(
            testo = "Controllare che tutti i DPI siano disponibili e funzionanti",
            tipo = TipoNota.SICUREZZA,
            categoria = "Sicurezza"
        ),
        TemplateNota(
            testo = "Pulizia e manutenzione straordinaria dell'area di lavoro",
            tipo = TipoNota.GENERALE,
            categoria = "Manutenzione"
        )
    )

    fun getByCategoria(categoria: String): List<TemplateNota> {
        return templates.filter { it.categoria == categoria }
    }

    fun getByTipo(tipo: TipoNota): List<TemplateNota> {
        return templates.filter { it.tipo == tipo }
    }
}