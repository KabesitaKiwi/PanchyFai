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
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home -> if (this !is Home) Home::class.java else null
                R.id.nav_songs -> if (this !is Songs) Songs::class.java else null
                R.id.nav_stats -> if (this !is Stats) Stats::class.java else null
                R.id.nav_comments -> if (this !is Comments) Comments::class.java else null
                R.id.nav_friends -> if (this !is Friends) Friends::class.java else null
                else -> null
            }
            if (targetClass != null) {
                val targetIntent = android.content.Intent(this, targetClass)
                targetIntent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(targetIntent)
                overridePendingTransition(0, 0)
            }
            true
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

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_songs)?.isChecked = true
    }
}
