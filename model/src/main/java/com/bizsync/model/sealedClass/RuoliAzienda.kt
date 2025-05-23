package com.bizsync.model.sealedClass


sealed class RuoliAzienda(val route: String, val isPrivileged: Boolean) {
    object Manager      : RuoliAzienda("manager", true)
    object Proprietario : RuoliAzienda("proprietario", true)
    object Impiegato    : RuoliAzienda("impiegato", false)
    object Contabile    : RuoliAzienda("contabile", false)
    object Sicurezza    : RuoliAzienda("sicurezza", false)
}
