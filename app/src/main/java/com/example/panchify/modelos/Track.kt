package com.example.panchify.modelos

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val duration_ms: Long = 0
)

data class Artist(
    val name: String
)

data class Album(
    val name: String,
    val images: List<Image>
)

data class Image(
    val url: String
)
