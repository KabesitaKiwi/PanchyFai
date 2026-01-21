package com.example.panchify.api

import com.example.panchify.modelos.TopTracksResponse
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

    @GET("v1/me/player/recently-played")
    fun getRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Call<com.example.panchify.vistas.RecentlyPlayedResponse>
}
