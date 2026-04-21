package com.example.panchify.api

import com.example.panchify.modelos.TopArtistsResponse
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.modelos.AudioFeaturesResponse
import com.example.panchify.modelos.ArtistsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
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
}
