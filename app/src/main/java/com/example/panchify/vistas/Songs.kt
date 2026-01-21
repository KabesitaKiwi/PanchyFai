package com.example.panchify.vistas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.preferences.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Stats : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        sessionManager = SessionManager(this)

        // Carga inicial: 4 semanas
        cargarCanciones("short_term")
    }

    private fun cargarCanciones(timeRange: String) {
        val token = sessionManager.getAccessToken() ?: return

        RetrofitClient.spotifyApiService.getTopTracks(
            authHeader = "Bearer $token",
            timeRange = timeRange
        ).enqueue(object : Callback<TopTracksResponse> {

            override fun onResponse(
                call: Call<TopTracksResponse>,
                response: Response<TopTracksResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val canciones = response.body()!!.items
                    // Aquí luego conectamos el RecyclerView
                }
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                // Error de red
            }
        })
    }
}
