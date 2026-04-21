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
import com.example.panchify.modelos.TopArtistsResponse
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
       cargarTarjetasExplorar()
       configurarClicksTarjetas()

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

    private fun configurarClicksTarjetas() {
        findViewById<android.view.View>(R.id.cardCanciones).setOnClickListener {
            irASongsTab(0)
        }
        findViewById<android.view.View>(R.id.cardArtistas).setOnClickListener {
            irASongsTab(1)
        }
        findViewById<android.view.View>(R.id.cardAlbumes).setOnClickListener {
            irASongsTab(2)
        }
        findViewById<android.view.View>(R.id.cardGeneros).setOnClickListener {
            irASongsTab(3)
        }
    }

    private fun irASongsTab(tabIndex: Int) {
        val intent = Intent(this, Songs::class.java)
        intent.putExtra("TAB_INDEX", tabIndex)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun cargarTarjetasExplorar() {
        val token = SessionManager(this).getAccessToken() ?: return

        // 1. Top Canciones (limit=4)
        RetrofitClient.spotifyApiService.getTopTracks("Bearer $token", "short_term", 4)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!.items
                        if (items.size > 0) cargarImagenSegura(items[0].album.images.firstOrNull()?.url, R.id.imgSong1)
                        if (items.size > 1) cargarImagenSegura(items[1].album.images.firstOrNull()?.url, R.id.imgSong2)
                        if (items.size > 2) cargarImagenSegura(items[2].album.images.firstOrNull()?.url, R.id.imgSong3)
                        if (items.size > 3) cargarImagenSegura(items[3].album.images.firstOrNull()?.url, R.id.imgSong4)
                    }
                }
                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {}
            })

        // 2. Top Artistas (limit=4)
        RetrofitClient.spotifyApiService.getTopArtists("Bearer $token", "short_term", 4)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!.items
                        if (items.size > 0) cargarImagenSegura(items[0].images.firstOrNull()?.url, R.id.imgArt1)
                        if (items.size > 1) cargarImagenSegura(items[1].images.firstOrNull()?.url, R.id.imgArt2)
                        if (items.size > 2) cargarImagenSegura(items[2].images.firstOrNull()?.url, R.id.imgArt3)
                        if (items.size > 3) cargarImagenSegura(items[3].images.firstOrNull()?.url, R.id.imgArt4)
                    }
                }
                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {}
            })

        // 3 y 4. Para Álbumes y Géneros necesitamos más datos (limit=50)
        RetrofitClient.spotifyApiService.getTopArtists("Bearer $token", "short_term", 50)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!.items
                        
                        // Géneros: Extraer los 4 géneros más repetidos
                        val genreImageMap = mutableMapOf<String, String>() // género -> imagen representativa
                        val genreCounts = mutableMapOf<String, Int>()
                        
                        items.forEach { artist ->
                            val imageUrl = artist.images.firstOrNull()?.url ?: ""
                            artist.genres.forEach { genre ->
                                genreCounts[genre] = genreCounts.getOrDefault(genre, 0) + 1
                                if (!genreImageMap.containsKey(genre) && imageUrl.isNotEmpty()) {
                                    genreImageMap[genre] = imageUrl
                                }
                            }
                        }
                        
                        val topGenres = genreCounts.entries.sortedByDescending { it.value }.take(4).map { it.key }
                        
                        if (topGenres.size > 0) cargarImagenSegura(genreImageMap[topGenres[0]], R.id.imgGen1)
                        if (topGenres.size > 1) cargarImagenSegura(genreImageMap[topGenres[1]], R.id.imgGen2)
                        if (topGenres.size > 2) cargarImagenSegura(genreImageMap[topGenres[2]], R.id.imgGen3)
                        if (topGenres.size > 3) cargarImagenSegura(genreImageMap[topGenres[3]], R.id.imgGen4)
                    }
                }
                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {}
            })

        RetrofitClient.spotifyApiService.getTopTracks("Bearer $token", "short_term", 50)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val items = response.body()!!.items
                        
                        // Álbumes: Extraer los 4 álbumes únicos más escuchados (por aparición en top tracks)
                        val albumUrls = mutableListOf<String>()
                        val albumNames = mutableSetOf<String>()
                        
                        for (track in items) {
                            if (!albumNames.contains(track.album.name)) {
                                albumNames.add(track.album.name)
                                track.album.images.firstOrNull()?.url?.let { albumUrls.add(it) }
                                if (albumUrls.size == 4) break
                            }
                        }
                        
                        if (albumUrls.size > 0) cargarImagenSegura(albumUrls[0], R.id.imgAlbum1)
                        if (albumUrls.size > 1) cargarImagenSegura(albumUrls[1], R.id.imgAlbum2)
                        if (albumUrls.size > 2) cargarImagenSegura(albumUrls[2], R.id.imgAlbum3)
                        if (albumUrls.size > 3) cargarImagenSegura(albumUrls[3], R.id.imgAlbum4)
                    }
                }
                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {}
            })
    }

    private fun cargarImagenSegura(url: String?, imageViewId: Int) {
        if (url != null) {
            val imgView = findViewById<android.widget.ImageView>(imageViewId)
            com.bumptech.glide.Glide.with(this).load(url).into(imgView)
        }
    }

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
