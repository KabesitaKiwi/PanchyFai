package com.example.panchify.modelos

data class TopTracksResponse(
    val items: List<Track>
)

data class ArtistTopTracksResponse(
    val tracks: List<Track>
)
