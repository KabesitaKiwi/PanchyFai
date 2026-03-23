package com.example.panchify.modelos

data class CurrentlyPlayingResponse(
    val is_playing: Boolean,
    val item: Track?
)
