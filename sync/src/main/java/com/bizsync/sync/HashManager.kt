package com.bizsync.sync

import java.security.MessageDigest
import kotlin.text.format

object HashManager {

    fun generateHash(data: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun <T> generateHashFromList(
        items: List<T>,
        keyExtractor: (T) -> String
    ): String {
        val sortedKeys = items
            .map { keyExtractor(it) }
            .sorted()
            .joinToString("|")

        return generateHash(sortedKeys)
    }
}