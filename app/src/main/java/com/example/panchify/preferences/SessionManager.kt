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

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ── Usuario MySQL (backend Hostinger) ────────────────────────────────────

    fun saveUserId(idUsuario: Int) {
        prefs.edit().putInt("id_usuario_mysql", idUsuario).apply()
    }

    fun getUserId(): Int? {
        val id = prefs.getInt("id_usuario_mysql", -1)
        return if (id == -1) null else id
    }

    fun saveSpotifyId(spotifyId: String) {
        prefs.edit().putString("spotify_id", spotifyId).apply()
    }

    fun getSpotifyId(): String? {
        return prefs.getString("spotify_id", null)
    }
}
