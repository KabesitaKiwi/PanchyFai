package com.example.panchify.config

object SpotifyConfig {

    const val CLIENT_ID = "7169289ba7de4350b0ef5105ace1e25f"

    const val REDIRECT_URI = "panchify://callback"

    val SCOPES = arrayOf(
        "user-read-private",
        "user-read-email",
        "user-top-read",
        "playlist-read-private",
        "playlist-modify-private",
        "playlist-modify-public"
    )
}