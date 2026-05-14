package com.example.panchify.modelos

data class PlaybackContextRequest(
    val context_uri: String,
    val offset: PlaybackOffset? = null,
    val position_ms: Int = 0
)

data class PlaybackOffset(
    val uri: String
)
