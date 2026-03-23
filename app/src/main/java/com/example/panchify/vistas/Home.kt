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
import com.example.panchify.modelos.Track
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class RecentlyPlayedResponse(
    val items: List<PlayHistoryItem>
)
data class PlayHistoryItem(
    val track: Track,
    val played_at: String
)
class Home : AppCompatActivity() {

    private lateinit var recyclerTopTracks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Setup RecyclerView
        recyclerTopTracks = findViewById(R.id.recyclerTopTracks)
        recyclerTopTracks.layoutManager = LinearLayoutManager(this)

        // Cargar datos
       cargarReproducidoUltimamente()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

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

    }

    /*private fun cargarTopTracks() {
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
     */

    private fun cargarReproducidoUltimamente() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getAccessToken()

        if (token != null) {
            // NOTA: Si 'getRecentlyPlayed' sale en ROJO, lee las instrucciones de abajo
            RetrofitClient.spotifyApiService.getRecentlyPlayed(
                token = "Bearer $token",
                limit = 20
            ).enqueue(object : Callback<RecentlyPlayedResponse> {
                override fun onResponse(
                    call: Call<RecentlyPlayedResponse>,
                    response: Response<RecentlyPlayedResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val historyItems = response.body()!!.items

                        // Convertimos el historial a lista de canciones para usar tu adaptador
                        val tracks = historyItems.map { it.track }

                        val adapter = TopTracksAdapter(tracks)
                        recyclerTopTracks.adapter = adapter
                    } else {
                        Log.e("API", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<RecentlyPlayedResponse>, t: Throwable) {
                    Log.e("API", "Error de red", t)
                }
            })
        } else {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun cargarEscuchandoAhora() {
        val sessionManager = com.example.panchify.preferences.SessionManager(this)
        val token = sessionManager.getAccessToken()

        if (token != null) {
            com.example.panchify.api.RetrofitClient.spotifyApiService.getCurrentlyPlaying("Bearer $token")
                .enqueue(object : retrofit2.Callback<com.example.panchify.modelos.CurrentlyPlayingResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.panchify.modelos.CurrentlyPlayingResponse>,
                        response: retrofit2.Response<com.example.panchify.modelos.CurrentlyPlayingResponse>
                    ) {
                        val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCurrentlyPlaying)
                        val title = findViewById<android.widget.TextView>(R.id.txtTitleCurrentlyPlaying)
                        if (response.isSuccessful && response.body() != null && response.body()!!.is_playing && response.body()!!.item != null) {
                            val track = response.body()!!.item!!
                            findViewById<android.widget.TextView>(R.id.txtCurrentSong).text = track.name
                            findViewById<android.widget.TextView>(R.id.txtCurrentArtist).text = track.artists.joinToString(", ") { it.name }
                            
                            val imgView = findViewById<android.widget.ImageView>(R.id.tarjetaEscuchandoAhora)
                            if (track.album.images.isNotEmpty()) {
                                com.bumptech.glide.Glide.with(this@Home)
                                    .load(track.album.images[0].url)
                                    .into(imgView)
                            }
                            card.visibility = android.view.View.VISIBLE
                            title.visibility = android.view.View.VISIBLE
                        } else {
                            card.visibility = android.view.View.GONE
                            title.visibility = android.view.View.GONE
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<com.example.panchify.modelos.CurrentlyPlayingResponse>, t: Throwable) {
                        // Ignorar fallo de red
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_home)?.isChecked = true
        cargarEscuchandoAhora()
    }
}
