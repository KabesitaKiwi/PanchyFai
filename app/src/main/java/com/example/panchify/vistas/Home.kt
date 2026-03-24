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

import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState


data class RecentlyPlayedResponse(
    val items: List<PlayHistoryItem>
)
data class PlayHistoryItem(
    val track: Track,
    val played_at: String
)
class Home : AppCompatActivity() {

    private lateinit var recyclerTopTracks: RecyclerView
    private var mSpotifyAppRemote: SpotifyAppRemote? = null

    private val CLIENT_ID = "7169289ba7de4350b0ef5105ace1e25f"
    private val REDIRECT_URI = "panchify://callback"

    private fun conectarAppRemote() {

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(false) // ponerlo en flase para que salga la autorizacion y que los botones y canciones se actualicen automaticamente a tiempo real
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                mSpotifyAppRemote = appRemote
                Log.d("Home", "Conectado a Spotify")

                cambioCancion();
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("Home", "Error al conectar a Spotify", throwable)
                if (throwable is com.spotify.android.appremote.api.error.UserNotAuthorizedException ||
                    throwable.message?.contains("Explicit user authorization is required") == true) {
                    
                    android.widget.Toast.makeText(this@Home, "Forzando pantalla de permisos...", android.widget.Toast.LENGTH_SHORT).show()
                    
                    val request = com.spotify.sdk.android.auth.AuthorizationRequest.Builder(
                        CLIENT_ID, com.spotify.sdk.android.auth.AuthorizationResponse.Type.TOKEN, REDIRECT_URI
                    ).setScopes(arrayOf("app-remote-control")).build()
                    
                    com.spotify.sdk.android.auth.AuthorizationClient.openLoginActivity(this@Home, 1337, request)
                } else {
                    android.widget.Toast.makeText(this@Home, "ERROR: ${throwable.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        conectarAppRemote()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == 1337) {
            val response = com.spotify.sdk.android.auth.AuthorizationClient.getResponse(resultCode, intent)
            if (response.type == com.spotify.sdk.android.auth.AuthorizationResponse.Type.TOKEN) {
                android.widget.Toast.makeText(this, "Permiso concedido. Reconectando...", android.widget.Toast.LENGTH_SHORT).show()
                conectarAppRemote()
            } else if (response.type == com.spotify.sdk.android.auth.AuthorizationResponse.Type.ERROR) {
                android.widget.Toast.makeText(this, "Error de permisos: ${response.error}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Setup RecyclerView
        recyclerTopTracks = findViewById(R.id.recyclerTopTracks)
        recyclerTopTracks.layoutManager = LinearLayoutManager(this)

        // Cargar datos
       cargarReproducidoUltimamente()
       configurarBotonesReproduccion()

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
                        if (response.isSuccessful && response.body() != null && response.body()!!.item != null) {
                            val track = response.body()!!.item!!
                            findViewById<android.widget.TextView>(R.id.txtCurrentSong).text = track.name
                            findViewById<android.widget.TextView>(R.id.txtCurrentArtist).text = track.artists.joinToString(", ") { it.name }
                            
                            val btnPlayPause = findViewById<android.widget.ImageButton>(R.id.btnPlayPause)
                            if (response.body()!!.is_playing) {
                                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                            } else {
                                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                            }
                            
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

    private fun configurarBotonesReproduccion() {
        val btnPrev = findViewById<android.widget.ImageButton>(R.id.btnPrev)
        val btnPlayPause = findViewById<android.widget.ImageButton>(R.id.btnPlayPause)
        val btnNext = findViewById<android.widget.ImageButton>(R.id.btnNext)

        btnPrev.setOnClickListener {
            if (mSpotifyAppRemote == null) {
                android.widget.Toast.makeText(this, "Botón pulsado pero no hay conexión a Spotify", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                mSpotifyAppRemote?.playerApi?.skipPrevious()
            }
        }
        btnPlayPause.setOnClickListener {
            if (mSpotifyAppRemote == null) {
                android.widget.Toast.makeText(this, "Botón pulsado pero no hay conexión a Spotify", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                mSpotifyAppRemote?.playerApi?.playerState?.setResultCallback { state ->
                    if (state.isPaused) {
                        mSpotifyAppRemote?.playerApi?.resume()
                    } else {
                        mSpotifyAppRemote?.playerApi?.pause()
                    }
                }
            }
        }
        btnNext.setOnClickListener {
            if (mSpotifyAppRemote == null) {
                android.widget.Toast.makeText(this, "Botón pulsado pero no hay conexión a Spotify", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                mSpotifyAppRemote?.playerApi?.skipNext()
            }
        }
    }

    private fun cambioCancion(){
        mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val track = playerState.track
            if (track != null) {
                val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCurrentlyPlaying)
                val titleText = findViewById<android.widget.TextView>(R.id.txtTitleCurrentlyPlaying)
                val songName = findViewById<android.widget.TextView>(R.id.txtCurrentSong)
                val artistName = findViewById<android.widget.TextView>(R.id.txtCurrentArtist)
                val imgView = findViewById<android.widget.ImageView>(R.id.tarjetaEscuchandoAhora)
                val btnPlayPause = findViewById<android.widget.ImageButton>(R.id.btnPlayPause)
                
                // Hacemos visible la tarjeta
                card.visibility = android.view.View.VISIBLE
                titleText.visibility = android.view.View.VISIBLE
                
                if (playerState.isPaused) {
                    songName.text = "${track.name} "
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    songName.text = track.name
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                }
                
                artistName.text = track.artist.name
                
                mSpotifyAppRemote?.imagesApi?.getImage(track.imageUri)?.setResultCallback { bitmap ->
                    imgView.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Desconectar al salir para ahorrar batería
        mSpotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}
