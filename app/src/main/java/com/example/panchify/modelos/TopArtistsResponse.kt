package com.example.panchify.modelos

data class TopArtistsResponse(
    val items: List<ArtistFull>
)

data class ArtistsResponse(
    val artists: List<ArtistFull>
)

data class ArtistFull(
    val id: String,
    val name: String,
    val genres: List<String> = emptyList(),
    val images: List<Image> = emptyList(),
    val popularity: Int = 0,
    val external_urls: ExternalUrls? = null
)
