package com.example.panchify.vistas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.CancionesAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.preferences.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Stats : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CancionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        sessionManager = SessionManager(this)

        recyclerView = findViewById(R.id.listaCanciones)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = CancionesAdapter(emptyList())
        recyclerView.adapter = adapter

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
                    adapter.actualizarDatos(response.body()!!.items)
                }
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                // Error de red
            }
        })
    }
}
