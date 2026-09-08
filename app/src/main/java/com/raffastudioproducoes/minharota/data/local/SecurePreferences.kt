package com.raffastudioproducoes.minharota.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Single encrypted preference store for local user data and cache. */
object SecurePreferences {
    private const val NAME = "minha_rota_secure_prefs"

    fun get(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encrypted = EncryptedSharedPreferences.create(
            appContext,
            NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        migrateLegacy(appContext, encrypted)
        return encrypted
    }

    private fun migrateLegacy(context: Context, encrypted: SharedPreferences) {
        val legacy = context.getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE)
        if (legacy.getBoolean("secure_migrated", false) || legacy.all.isEmpty()) return
        val editor = encrypted.edit()
        legacy.all.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.putBoolean("secure_migrated", true)
        if (editor.commit()) {
            // Remove the plaintext copy only after the encrypted commit succeeds.
            legacy.edit().clear().commit()
        }
    }
}
