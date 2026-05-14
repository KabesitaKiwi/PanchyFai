package com.example.panchify.api

import com.example.panchify.modelos.TopArtistsResponse
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.modelos.AudioFeaturesResponse
import com.example.panchify.modelos.ArtistTopTracksResponse
import com.example.panchify.modelos.ArtistsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpotifyApiService {

    @GET("v1/me/top/tracks")
    fun getTopTracks(
        @Header("Authorization") authHeader: String,
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int = 20
    ): Call<TopTracksResponse>

    @GET("v1/me/top/artists")
    fun getTopArtists(
        @Header("Authorization") authHeader: String,
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int = 20
    ): Call<TopArtistsResponse>

    @GET("v1/artists")
    fun getArtists(
        @Header("Authorization") authHeader: String,
        @Query("ids") ids: String
    ): Call<ArtistsResponse>

    @GET("v1/artists/{artistId}/top-tracks")
    fun getArtistTopTracks(
        @Header("Authorization") authHeader: String,
        @Path("artistId") artistId: String,
        @Query("market") market: String = "ES"
    ): Call<ArtistTopTracksResponse>

    @GET("v1/audio-features")
    fun getAudioFeatures(
        @Header("Authorization") authHeader: String,
        @Query("ids") ids: String
    ): Call<AudioFeaturesResponse>

    @GET("v1/me/player/recently-played")
    fun getRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Call<com.example.panchify.vistas.RecentlyPlayedResponse>

    @GET("v1/me/player/currently-playing")
    fun getCurrentlyPlaying(
        @Header("Authorization") token: String
    ): Call<com.example.panchify.modelos.CurrentlyPlayingResponse>

    @PUT("v1/me/player/play")
    fun playInContext(
        @Header("Authorization") authHeader: String,
        @Body request: com.example.panchify.modelos.PlaybackContextRequest
    ): Call<Void>

    @GET("v1/me")
    fun getProfile(
        @Header("Authorization") token: String
    ): Call<com.example.panchify.modelos.UserProfileResponse>

    @GET("v1/search")
    fun searchTracks(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 20
    ): Call<com.example.panchify.modelos.SearchResponse>

    @GET("v1/me/playlists")
    fun getMyPlaylists(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Call<com.example.panchify.modelos.SpotifyPlaylistsResponse>

    @GET("v1/playlists/{playlistId}/tracks")
    fun getPlaylistTracks(
        @Header("Authorization") authHeader: String,
        @Path("playlistId") playlistId: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Call<com.example.panchify.modelos.SpotifyPlaylistTracksResponse>
}
