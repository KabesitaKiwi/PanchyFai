package com.example.panchify.modelos

data class SpotifyPlaylistsResponse(
    val items: List<SpotifyPlaylistSimple>,
    val total: Int = 0
)

data class SpotifyPlaylistSimple(
    val id: String,
    val name: String,
    val description: String? = null,
    val images: List<Image> = emptyList(),
    val tracks: SpotifyPlaylistTracksInfo,
    val owner: SpotifyPlaylistOwner? = null
)

data class SpotifyPlaylistTracksInfo(
    val total: Int
)

data class SpotifyPlaylistOwner(
    val id: String,
    val display_name: String? = null
)

data class SpotifyPlaylistTracksResponse(
    val items: List<SpotifyPlaylistTrackItem>,
    val total: Int = 0
)

data class SpotifyPlaylistTrackItem(
    val track: Track?
)
