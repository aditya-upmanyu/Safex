package com.safex.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object DbPassphraseProvider {
    
    private const val KEYSTORE_ALIAS = "safex_db_key"
    private const val PREFS_FILENAME = "safex_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphrase = sharedPreferences.getString(KEY_DB_PASSPHRASE, null)
        
        if (passphrase == null) {
            passphrase = generateSecurePassphrase()
            sharedPreferences.edit()
                .putString(KEY_DB_PASSPHRASE, passphrase)
                .apply()
        }

        return passphrase.toByteArray(Charsets.UTF_8)
    }

    private fun generateSecurePassphrase(): String {
        // Generate a 256-bit cryptographically secure random passphrase
        val secureRandom = java.security.SecureRandom()
        val keyBytes = ByteArray(32)
        secureRandom.nextBytes(keyBytes)
        return keyBytes.joinToString("") { "%02x".format(it) }
    }

    fun clearPassphrase(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit().remove(KEY_DB_PASSPHRASE).apply()

        // Also delete from KeyStore
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
