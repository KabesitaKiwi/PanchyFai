package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class Home : AppCompatActivity() {

    private lateinit var recyclerTopTracks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        recyclerTopTracks = findViewById(R.id.recyclerTopTracks)
        recyclerTopTracks.layoutManager = LinearLayoutManager(this)

        // Cargar datos
        cargarTopTracks()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_songs -> {
                    val intent = Intent(this, Songs::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> {
                    // acción search
                    val intent = Intent(this, Stats::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_comments -> {
                    // acción library
                    val intent = Intent(this, Comments::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_friends -> {
                    // acción profile
                    val intent = Intent(this, Friends::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    private fun cargarTopTracks() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getAccessToken()

        if (token != null) {
            RetrofitClient.spotifyApiService.getTopTracks(
                "Bearer $token",
                "short_term" // ultimas 4 semanas
            ).enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(
                    call: Call<TopTracksResponse>,
                    response: Response<TopTracksResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val tracks = response.body()!!.items
                        val adapter = TopTracksAdapter(tracks)
                        recyclerTopTracks.adapter = adapter
                    } else {
                        Log.e("Home", "Error en respuesta API: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Log.e("Home", "Fallo en llamada API", t)
                }
            })
        } else {
            // Manejar caso no logueado redirigiendo a Login o mostrando error
             val intent = Intent(this, Login::class.java)
             startActivity(intent)
             finish()
        }
    }
}
