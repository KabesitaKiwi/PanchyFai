package com.example.panchify.modelos

data class TopArtistsResponse(
    val items: List<ArtistFull>
)

data class ArtistFull(
    val id: String,
    val name: String,
    val genres: List<String>,
    val images: List<Image>
)
