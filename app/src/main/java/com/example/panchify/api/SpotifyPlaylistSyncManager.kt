package com.example.panchify.api

import android.content.Context
import android.util.Log
import com.example.panchify.db.ArtistaDao
import com.example.panchify.db.PlaylistDao
import com.example.panchify.modelos.CancionBD
import com.example.panchify.modelos.SpotifyPlaylistTracksResponse
import com.example.panchify.modelos.Track
import com.example.panchify.preferences.SessionManager

object SpotifyPlaylistSyncManager {

    @Volatile
    private var isSyncing = false

    fun syncAll(context: Context) {
        if (isSyncing) return

        val sessionManager = SessionManager(context.applicationContext)
        val token = sessionManager.getAccessToken() ?: return
        val idUsuario = sessionManager.getUserId() ?: return

        isSyncing = true
        Thread {
            try {
                val playlistsResponse = RetrofitClient.spotifyApiService
                    .getMyPlaylists("Bearer $token")
                    .execute()

                val playlists = cargarTodasLasPlaylists(token, playlistsResponse.body())
                playlists.forEach { playlist ->
                    PlaylistDao.guardarPlaylistSpotify(
                        playlist.id,
                        playlist.name,
                        playlist.description,
                        idUsuario
                    )

                    val tracksResponse = cargarTodasLasCancionesPlaylist(token, playlist.id)

                    val canciones = convertirTracksSpotify(tracksResponse)
                    PlaylistDao.guardarCancionesPlaylistSpotify(
                        idUsuario,
                        playlist.id,
                        playlist.name,
                        playlist.description,
                        canciones
                    )
                    guardarArtistasYRelaciones(tracksResponse, token)
                }
            } catch (e: Exception) {
                Log.e("PlaylistSync", "Error sincronizando playlists", e)
            } finally {
                isSyncing = false
            }
        }.start()
    }

    fun cargarTodasLasCancionesPlaylist(token: String, playlistId: String): SpotifyPlaylistTracksResponse {
        val allItems = mutableListOf<com.example.panchify.modelos.SpotifyPlaylistTrackItem>()
        val limit = 100
        var offset = 0
        var total = Int.MAX_VALUE

        while (offset < total) {
            val response = RetrofitClient.spotifyApiService.getPlaylistTracks(
                authHeader = "Bearer $token",
                playlistId = playlistId,
                limit = limit,
                offset = offset
            ).execute()

            if (!response.isSuccessful || response.body() == null) break

            val body = response.body()!!
            allItems.addAll(body.items)
            total = body.total.takeIf { it > 0 } ?: allItems.size
            if (body.items.isEmpty()) break
            offset += body.items.size
        }

        return SpotifyPlaylistTracksResponse(allItems, allItems.size)
    }

    fun cargarTodasLasPlaylists(
        token: String,
        firstPage: com.example.panchify.modelos.SpotifyPlaylistsResponse?
    ): List<com.example.panchify.modelos.SpotifyPlaylistSimple> {
        val limit = 50
        val initialPage = firstPage ?: RetrofitClient.spotifyApiService.getMyPlaylists(
            authHeader = "Bearer $token",
            limit = limit,
            offset = 0
        ).execute().body()

        val playlists = initialPage?.items.orEmpty().toMutableList()
        var offset = playlists.size
        val total = initialPage?.total?.takeIf { it > 0 } ?: playlists.size

        while (offset < total) {
            val response = RetrofitClient.spotifyApiService.getMyPlaylists(
                authHeader = "Bearer $token",
                limit = limit,
                offset = offset
            ).execute()

            if (!response.isSuccessful || response.body() == null) break

            val items = response.body()!!.items
            if (items.isEmpty()) break
            playlists.addAll(items)
            offset += items.size
        }

        return playlists
    }

    fun syncTracks(tracksResponse: SpotifyPlaylistTracksResponse, token: String) {
        Thread {
            guardarArtistasYRelaciones(tracksResponse, token)
        }.start()
    }

    private fun convertirTracksSpotify(response: SpotifyPlaylistTracksResponse): List<CancionBD> {
        return response.items
            .mapNotNull { it.track }
            .map { track ->
                CancionBD(
                    idCancion = track.id,
                    titulo = track.name,
                    artista = track.artists.joinToString(", ") { it.name },
                    album = track.album.name,
                    imagenUrl = track.album.images.firstOrNull()?.url,
                    previewUrl = track.preview_url,
                    duracionMs = track.duration_ms,
                    anio = track.album.release_date?.take(4),
                    spotifyUrl = track.external_urls?.spotify
                )
            }
    }

    private fun guardarArtistasYRelaciones(
        response: SpotifyPlaylistTracksResponse,
        token: String
    ) {
        val tracks = response.items.mapNotNull { it.track }
        val artistasBasicos = tracks
            .flatMap { it.artists }
            .distinctBy { it.id }
            .filter { it.id.isNotBlank() }

        ArtistaDao.guardarArtistasBasicos(artistasBasicos)
        ArtistaDao.guardarRelacionesCancionArtistas(tracks)
        guardarDetallesArtistas(artistasBasicos.map { it.id }, token)
    }

    private fun guardarDetallesArtistas(artistIds: List<String>, token: String) {
        artistIds
            .distinct()
            .chunked(50)
            .forEach { ids ->
                try {
                    val response = RetrofitClient.spotifyApiService.getArtists(
                        authHeader = "Bearer $token",
                        ids = ids.joinToString(",")
                    ).execute()

                    if (response.isSuccessful && response.body() != null) {
                        ArtistaDao.guardarArtistas(response.body()!!.artists)
                    }
                } catch (e: Exception) {
                    Log.e("PlaylistSync", "Error guardando detalles de artistas", e)
                }
            }
    }
}
