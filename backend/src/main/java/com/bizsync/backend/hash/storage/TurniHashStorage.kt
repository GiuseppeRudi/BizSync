package com.bizsync.backend.hash.storage


import com.bizsync.backend.hash.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurniHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    fun saveTurniHash(idAzienda: String, hash: String) {
        hashStorage.saveHash("turni_$idAzienda", hash)
    }

    fun getTurniHash(idAzienda: String): String? {
        return hashStorage.getHash("turni_$idAzienda")
    }

    fun deleteTurniHash(idAzienda: String) {
        hashStorage.deleteHash("turni_$idAzienda")
    }

    // Metodi aggiuntivi utili per i turni
    fun saveLastSyncTime(idAzienda: String, timestamp: Long) {
        hashStorage.saveHash("turni_last_sync_$idAzienda", timestamp.toString())
    }

    fun getLastSyncTime(idAzienda: String): Long? {
        return hashStorage.getHash("turni_last_sync_$idAzienda")?.toLongOrNull()
    }

    fun deleteLastSyncTime(idAzienda: String) {
        hashStorage.deleteHash("turni_last_sync_$idAzienda")
    }

    // Salva hash di un singolo turno (per update specifici)
    fun saveSingleTurnoHash(turnoId: String, hash: String) {
        hashStorage.saveHash("turno_single_$turnoId", hash)
    }

    fun getSingleTurnoHash(turnoId: String): String? {
        return hashStorage.getHash("turno_single_$turnoId")
    }

    fun deleteSingleTurnoHash(turnoId: String) {
        hashStorage.deleteHash("turno_single_$turnoId")
    }

    // Pulisce tutta la cache per i turni di un'azienda
    fun clearTurniCache(idAzienda: String) {
        deleteTurniHash(idAzienda)
        deleteLastSyncTime(idAzienda)
    }
}
