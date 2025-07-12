package com.bizsync.backend.hash.storage


import com.bizsync.backend.hash.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DipendentiHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    fun saveDipendentiHash(idAzienda: String, hash: String) {
        hashStorage.saveHash("dipendenti_$idAzienda", hash)
    }

    fun getDipendentiHash(idAzienda: String): String? {
        return hashStorage.getHash("dipendenti_$idAzienda")
    }

    fun deleteDipendentiHash(idAzienda: String) {
        hashStorage.deleteHash("dipendenti_$idAzienda")
    }

    // Metodi aggiuntivi utili per i dipendenti
    fun saveLastSyncTime(idAzienda: String, timestamp: Long) {
        hashStorage.saveHash("dipendenti_last_sync_$idAzienda", timestamp.toString())
    }

    fun getLastSyncTime(idAzienda: String): Long? {
        return hashStorage.getHash("dipendenti_last_sync_$idAzienda")?.toLongOrNull()
    }

    fun deleteLastSyncTime(idAzienda: String) {
        hashStorage.deleteHash("dipendenti_last_sync_$idAzienda")
    }

    // Pulisce tutta la cache per i dipendenti di un'azienda
    fun clearDipendentiCache(idAzienda: String) {
        deleteDipendentiHash(idAzienda)
        deleteLastSyncTime(idAzienda)
    }
}