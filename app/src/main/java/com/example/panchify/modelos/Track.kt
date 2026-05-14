package com.example.panchify.modelos

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val duration_ms: Long = 0,
    val popularity: Int = 0,
    val preview_url: String? = null,
    val external_urls: ExternalUrls? = null
)

data class Artist(
    val id: String = "",
    val name: String
)

data class Album @JvmOverloads constructor(
    val name: String,
    val images: List<Image>,
    val release_date: String? = null,
    val id: String = ""
)

data class Image(
    val url: String
)

data class ExternalUrls(
    val spotify: String? = null
)
