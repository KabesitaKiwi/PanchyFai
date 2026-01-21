package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.TopTracksAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.TopTracksResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Songs : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var listaCanciones: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        sessionManager = SessionManager(this)

        listaCanciones = findViewById(R.id.listaCanciones)
        listaCanciones.layoutManager = LinearLayoutManager(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_songs
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_songs -> {
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, Stats::class.java))
                    true
                }
                R.id.nav_comments -> {
                    startActivity(Intent(this, Comments::class.java))
                    true
                }
                R.id.nav_friends -> {
                    startActivity(Intent(this, Friends::class.java))
                    true
                }
                else -> false
            }
        }

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
                    val adapter = TopTracksAdapter(canciones)
                    listaCanciones.adapter = adapter
                }
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                // Error de red
            }
        })
    }
}
