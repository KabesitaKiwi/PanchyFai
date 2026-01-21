package com.example.panchify.preferences

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "panchify_session",
        Context.MODE_PRIVATE
    )

    fun saveToken(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Int
    ) {
        val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)

        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("token_expiration", expirationTime)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun isTokenExpired(): Boolean {
        val expiration = prefs.getLong("token_expiration", 0)
        return System.currentTimeMillis() > expiration
    }

    fun hasValidSession(): Boolean {
        val token = getAccessToken()
        return token != null && !isTokenExpired()
    }
}

