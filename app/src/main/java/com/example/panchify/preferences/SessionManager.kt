package com.example.panchify.preferences

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "panchify_session",
        Context.MODE_PRIVATE
    )

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean("is_logged_in", value).apply()
    }
}
