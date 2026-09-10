package com.kmj.ansik.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import java.util.UUID

object AuthSessionStore {

    private const val FILE_NAME = "ansik_secure_session"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_USER = "authUser"
    private const val KEY_DEVICE_ID = "deviceId"

    private lateinit var preferences: SharedPreferences
    private val gson = Gson()

    fun initialize(context: Context) {
        if (::preferences.isInitialized) return
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        preferences = EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(response: AuthResponse) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, response.accessToken)
            .putString(KEY_REFRESH_TOKEN, response.refreshToken)
            .putString(KEY_USER, gson.toJson(response.user))
            .apply()
    }

    fun accessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun refreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun currentUser(): AuthUser? = preferences.getString(KEY_USER, null)?.let { json ->
        runCatching { gson.fromJson(json, AuthUser::class.java) }.getOrNull()
    }

    fun isSignedIn(): Boolean = !accessToken().isNullOrBlank() && currentUser() != null

    fun deviceId(): String {
        preferences.getString(KEY_DEVICE_ID, null)?.let { return it }
        return UUID.randomUUID().toString().also {
            preferences.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    fun clear() {
        val deviceId = preferences.getString(KEY_DEVICE_ID, null)
        preferences.edit().clear().apply()
        if (deviceId != null) preferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }
}
