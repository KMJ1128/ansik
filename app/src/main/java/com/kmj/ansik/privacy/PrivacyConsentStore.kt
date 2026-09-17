package com.kmj.ansik.privacy

import android.content.Context
import android.content.SharedPreferences

object PrivacyConsentStore {
    const val CONSENT_VERSION = "2026-09-17"

    private const val FILE_NAME = "ansik_privacy_consent"
    private const val KEY_SENSITIVE_INFO_ACCEPTED = "sensitiveInfoAccepted"
    private const val KEY_SENSITIVE_INFO_VERSION = "sensitiveInfoVersion"

    private lateinit var preferences: SharedPreferences

    fun initialize(context: Context) {
        if (::preferences.isInitialized) return
        preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun hasSensitiveInfoConsent(): Boolean {
        if (!::preferences.isInitialized) return false
        return preferences.getBoolean(KEY_SENSITIVE_INFO_ACCEPTED, false) &&
            preferences.getString(KEY_SENSITIVE_INFO_VERSION, null) == CONSENT_VERSION
    }

    fun acceptSensitiveInfoConsent() {
        check(::preferences.isInitialized) { "PrivacyConsentStore is not initialized" }
        preferences.edit()
            .putBoolean(KEY_SENSITIVE_INFO_ACCEPTED, true)
            .putString(KEY_SENSITIVE_INFO_VERSION, CONSENT_VERSION)
            .apply()
    }

    fun revokeSensitiveInfoConsent() {
        if (!::preferences.isInitialized) return
        preferences.edit()
            .remove(KEY_SENSITIVE_INFO_ACCEPTED)
            .remove(KEY_SENSITIVE_INFO_VERSION)
            .apply()
    }
}
