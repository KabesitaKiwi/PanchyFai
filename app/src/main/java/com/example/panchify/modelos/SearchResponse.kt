package com.example.panchify.modelos

data class SearchResponse(
    val tracks: TracksPaging
)

data class TracksPaging(
    val items: List<Track>
)
