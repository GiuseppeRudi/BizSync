package com.bizsync.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "bizsync_hash_storage",
        Context.MODE_PRIVATE
    )

    fun saveHash(key: String, hash: String) {
        prefs.edit { putString(key, hash) }
    }

    fun getHash(key: String): String? {
        return prefs.getString(key, null)
    }

    fun deleteHash(key: String) {
        prefs.edit { remove(key) }
    }

    fun clearAllHashes() {
        prefs.edit { clear() }
    }

    fun getAllKeys(): Set<String> {
        return prefs.all.keys
    }

}