package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.PlaylistSongsAdapter
import com.example.panchify.adapters.PlaylistsAdapter
import com.example.panchify.api.RetrofitClient
import com.example.panchify.api.SpotifyPlaylistSyncManager
import com.example.panchify.db.ArtistaDao
import com.example.panchify.db.PlaylistDao
import com.example.panchify.modelos.CancionBD
import com.example.panchify.modelos.SpotifyPlaylistSimple
import com.example.panchify.modelos.SpotifyPlaylistTracksResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class Playlists : AppCompatActivity() {

    private lateinit var recyclerPlaylists: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtEmpty: TextView
    private lateinit var txtTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var sessionManager: SessionManager

    private var playlistSeleccionada: SpotifyPlaylistSimple? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        sessionManager = SessionManager(this)
        recyclerPlaylists = findViewById(R.id.recyclerPlaylists)
        progressBar = findViewById(R.id.progressPlaylists)
        txtEmpty = findViewById(R.id.txtEmptyPlaylists)
        txtTitle = findViewById(R.id.txtPlaylistsTitle)
        btnBack = findViewById(R.id.btnBackPlaylists)

        recyclerPlaylists.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener {
            if (playlistSeleccionada != null) {
                cargarPlaylists()
            } else {
                finish()
            }
        }

        configurarBottomNavigation()
        cargarPlaylists()
    }

    override fun onStart() {
        super.onStart()
        com.example.panchify.api.SpotifyRemoteManager.connect(this)
    }

    private fun cargarPlaylists() {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            Toast.makeText(this, "No se encontro la sesion de Spotify", Toast.LENGTH_SHORT).show()
            return
        }

        playlistSeleccionada = null
        txtTitle.text = "Playlists"
        mostrarCarga(true)

        Thread {
            try {
                val playlists = SpotifyPlaylistSyncManager.cargarTodasLasPlaylists(token, null)
                sincronizarPlaylistsYContenidoEnBD(playlists, token)

                runOnUiThread {
                    mostrarCarga(false)
                    txtEmpty.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
                    txtEmpty.text = "No tienes playlists en Spotify"
                    recyclerPlaylists.adapter = PlaylistsAdapter(playlists) { playlist ->
                        cargarCanciones(playlist)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    mostrarCarga(false)
                    txtEmpty.visibility = View.VISIBLE
                    txtEmpty.text = "Error cargando playlists de Spotify"
                }
            }
        }.start()
    }

    private fun cargarCanciones(playlist: SpotifyPlaylistSimple) {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            Toast.makeText(this, "No se encontro la sesion de Spotify", Toast.LENGTH_SHORT).show()
            return
        }

        playlistSeleccionada = playlist
        txtTitle.text = playlist.name
        mostrarCarga(true)

        Thread {
            try {
                val tracksResponse = SpotifyPlaylistSyncManager.cargarTodasLasCancionesPlaylist(token, playlist.id)
                val canciones = convertirTracksSpotify(tracksResponse)

                runOnUiThread {
                    mostrarCarga(false)
                    txtEmpty.visibility = if (canciones.isEmpty()) View.VISIBLE else View.GONE
                    txtEmpty.text = "Esta playlist no tiene canciones"
                    recyclerPlaylists.adapter = PlaylistSongsAdapter(canciones, playlist.id)
                }

                sincronizarPlaylistCompletaEnBD(playlist, canciones, tracksResponse, token)
            } catch (e: Exception) {
                runOnUiThread {
                    mostrarCarga(false)
                    txtEmpty.visibility = View.VISIBLE
                    txtEmpty.text = "Error cargando canciones"
                }
            }
        }.start()
    }

    private fun sincronizarPlaylistsYContenidoEnBD(
        playlists: List<SpotifyPlaylistSimple>,
        token: String
    ) {
        val idUsuario = sessionManager.getUserId() ?: return

        Thread {
            playlists.forEach { playlist ->
                PlaylistDao.guardarPlaylistSpotify(
                    playlist.id,
                    playlist.name,
                    playlist.description,
                    idUsuario
                )

                try {
                    val tracksResponse = SpotifyPlaylistSyncManager.cargarTodasLasCancionesPlaylist(token, playlist.id)
                    guardarPlaylistCancionesYArtistas(
                        idUsuario = idUsuario,
                        playlist = playlist,
                        tracksResponse = tracksResponse,
                        token = token
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun sincronizarPlaylistCompletaEnBD(
        playlist: SpotifyPlaylistSimple,
        canciones: List<CancionBD>,
        tracksResponse: SpotifyPlaylistTracksResponse,
        token: String
    ) {
        val idUsuario = sessionManager.getUserId() ?: return

        Thread {
            guardarPlaylistCancionesYArtistas(idUsuario, playlist, canciones, tracksResponse, token)
        }.start()
    }

    private fun guardarPlaylistCancionesYArtistas(
        idUsuario: Int,
        playlist: SpotifyPlaylistSimple,
        tracksResponse: SpotifyPlaylistTracksResponse,
        token: String
    ) {
        guardarPlaylistCancionesYArtistas(
            idUsuario = idUsuario,
            playlist = playlist,
            canciones = convertirTracksSpotify(tracksResponse),
            tracksResponse = tracksResponse,
            token = token
        )
    }

    private fun guardarPlaylistCancionesYArtistas(
        idUsuario: Int,
        playlist: SpotifyPlaylistSimple,
        canciones: List<CancionBD>,
        tracksResponse: SpotifyPlaylistTracksResponse,
        token: String
    ) {
        PlaylistDao.guardarCancionesPlaylistSpotify(
            idUsuario,
            playlist.id,
            playlist.name,
            playlist.description,
            canciones
        )
        guardarArtistasYRelaciones(tracksResponse, token)
    }

    private fun convertirTracksSpotify(response: SpotifyPlaylistTracksResponse): List<CancionBD> {
        return response.items
            .mapNotNull { it.track }
            .map { track ->
                CancionBD(
                    idCancion = track.id,
                    titulo = track.name,
                    artista = track.artists.joinToString(", ") { it.name },
                    album = track.album.name,
                    imagenUrl = track.album.images.firstOrNull()?.url,
                    previewUrl = track.preview_url,
                    duracionMs = track.duration_ms,
                    anio = track.album.release_date?.take(4),
                    spotifyUrl = track.external_urls?.spotify
                )
            }
    }

    private fun guardarArtistasYRelaciones(
        response: SpotifyPlaylistTracksResponse,
        token: String
    ) {
        val artistasBasicos = response.items
            .mapNotNull { it.track }
            .flatMap { it.artists }
            .distinctBy { it.id }
            .filter { it.id.isNotBlank() }

        ArtistaDao.guardarArtistasBasicos(artistasBasicos)
        ArtistaDao.guardarRelacionesCancionArtistas(response.items.mapNotNull { it.track })

        artistasBasicos
            .map { it.id }
            .chunked(50)
            .forEach { ids ->
                try {
                    val artistsResponse = RetrofitClient.spotifyApiService.getArtists(
                        authHeader = "Bearer $token",
                        ids = ids.joinToString(",")
                    ).execute()

                    if (artistsResponse.isSuccessful && artistsResponse.body() != null) {
                        ArtistaDao.guardarArtistas(artistsResponse.body()!!.artists)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        if (mostrar) txtEmpty.visibility = View.GONE
    }

    private fun configurarBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home -> Home::class.java
                R.id.nav_songs -> Songs::class.java
                R.id.nav_stats -> Stats::class.java
                R.id.nav_comments -> Comments::class.java
                R.id.nav_friends -> Friends::class.java
                else -> null
            }

            if (targetClass != null) {
                val intent = Intent(this, targetClass)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            true
        }
    }
}
